package br.com.cronicasdafronteira;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Mantém o formato JSON da versão 0.2 para preservar progresso em mundos existentes. */
public final class Progress {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, State> CACHE = new HashMap<>();
    private Progress() {}
    public static final class State {
        public int level = 1, xp = 0, completed = 0, kills = 0, honor = 0, hostileKills = 0;
        public boolean active = false, journalGiven = false;
        public String chapterDecision = "", ending = "", contractId = "";
        public int contractKills = 0;
        public Map<String, String> decisions = new HashMap<>();
        public Map<String, Integer> relations = new HashMap<>();
        public Map<String, Long> contractsCompletedAt = new HashMap<>();
        public boolean dungeonCreated = false, inDungeon = false;
        public int dungeonPhase = 0, returnX, returnY, returnZ, dungeonX, dungeonY, dungeonZ;
        public boolean bountyActive = false;
        public int bountyKills = 0;
        public transient int dialogNpcId = -1;
        public transient String dialogNpcUuid = "", dialogNode = "inicio", dialogRole = "other";
        public transient long dialogExpiresAt = 0;
    }
    public static int nextXp(int level) { return 75 + 60 * level; }
    private static Path file(ServerPlayer player) {
        return player.level().getServer().getWorldPath(LevelResource.ROOT).resolve("cronicasdafronteira")
            .resolve(player.getUUID().toString() + ".json");
    }
    public static State get(ServerPlayer player) {
        return CACHE.computeIfAbsent(player.getUUID(), key -> {
            try {
                Path f = file(player);
                if (Files.isRegularFile(f)) {
                    State s = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), State.class);
                    if (s != null) {
                        s.level = Math.max(1, s.level);
                        s.completed = Math.min(AdventureRules.STORY.size(), Math.max(0, s.completed));
                        s.honor = AdventureRules.clampHonor(s.honor);
                        if (s.decisions == null) s.decisions = new HashMap<>();
                        if (s.relations == null) s.relations = new HashMap<>();
                        if (s.contractsCompletedAt == null) s.contractsCompletedAt = new HashMap<>();
                        if (s.contractId == null) s.contractId = "";
                        if (s.ending == null) s.ending = "";
                        s.dialogNpcId = -1;
                        s.dialogNode = "inicio";
                        s.dialogRole = "other";
                        return s;
                    }
                }
            } catch (IOException | RuntimeException ex) {
                System.err.println("[Cronicas] Erro na leitura de " + key + ": " + ex.getMessage());
            }
            return new State();
        });
    }
    public static void save(ServerPlayer player) {
        try {
            Path f = file(player);
            Files.createDirectories(f.getParent());
            Path temp = f.resolveSibling(f.getFileName()+".tmp");
            Files.writeString(temp, GSON.toJson(get(player)), StandardCharsets.UTF_8);
            try { Files.move(temp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temp, f, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException ex) { System.err.println("[Cronicas] Erro ao salvar: " + ex.getMessage()); }
    }
    public static void clear() { CACHE.clear(); }
}
