package br.com.cronicasdafronteira;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.UUID;

/** Mod RPG Fabric 26.3: escolhas autenticadas no servidor, progresso por jogador e GUI cliente. */
public final class CronicasMod implements ModInitializer {
    public static final String ID = "cronicasdafronteira";
    public static Item DIARIO;
    public static final String CARCEREIRO = "Carcereiro Cinzento";
    public static final String DAMA = "Dama do Eclipse";

    @Override public void onInitialize() {
        Identifier id = Identifier.fromNamespaceAndPath(ID, "diario");
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        DIARIO = Registry.register(BuiltInRegistries.ITEM, key,
                new JournalItem(new Item.Properties().setId(key).stacksTo(1)));
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> registerCommands(dispatcher));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> Progress.clear());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer p = handler.player;
            Progress.State s = Progress.get(p);
            if (!s.journalGiven) {
                p.getInventory().add(new ItemStack(DIARIO));
                s.journalGiven = true;
                Progress.save(p);
                tell(p, "§6Crônicas da Fronteira: §7Diário recebido! Converse com os moradores sem agachar.");
            }
        });
        // O item e a GUI são ativados localmente. A sessão e as escolhas sempre são verificadas no servidor.
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || player.isSpectator() || level.isClientSide())
                return InteractionResult.PASS;
            if (entity instanceof Villager v && !player.isShiftKeyDown() && player instanceof ServerPlayer p) {
                begin(p, v.getId());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, damageSource) -> {
            if (!(damageSource.getEntity() instanceof ServerPlayer p)) return;
            Progress.State s = Progress.get(p);
            if (victim instanceof Villager) {
                changeHonor(p, -35, "A aldeia testemunhou um crime contra um morador.");
                s.relations.put(victim.getUUID().toString(), -100);
            } else if (victim instanceof IronGolem) changeHonor(p, -25, "Você destruiu um guardião da aldeia.");
            if (victim instanceof Monster) s.hostileKills++;
            String keyName = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
            if (s.active && s.completed < AdventureRules.STORY.size()) {
                AdventureRules.Quest q = AdventureRules.STORY.get(s.completed);
                boolean eligible = q.goal() == AdventureRules.Goal.KILL && q.target().equals(keyName);
                if (q.goal() == AdventureRules.Goal.BOSS && victim.getCustomName() != null) {
                    String boss = s.completed == 4 ? CARCEREIRO : DAMA;
                    int phase = s.completed == 4 ? 1 : 2;
                    eligible = s.dungeonPhase == phase && boss.equals(victim.getCustomName().getString());
                }
                if (eligible && s.kills < q.quantity()) {
                    s.kills++;
                    tell(p, "§6[História] " + q.title() + " §f" + s.kills + "/" + q.quantity());
                }
            }
            if (!s.contractId.isEmpty()) {
                AdventureRules.Quest side = side(s.contractId);
                if (side != null && side.goal() == AdventureRules.Goal.KILL && side.target().equals(keyName)
                        && s.contractKills < side.quantity()) {
                    s.contractKills++;
                    tell(p, "§b[Contrato] " + side.title() + " §f" + s.contractKills + "/" + side.quantity());
                }
            }
            Progress.save(p);
        });
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cronicas")
            .executes(ctx -> journal(ctx.getSource().getPlayerOrException()))
            .then(Commands.literal("diario").executes(ctx -> journal(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("honra").executes(ctx -> honor(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("iniciar").then(Commands.argument("id", IntegerArgumentType.integer(0))
                .executes(ctx -> begin(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx,"id")))))
            .then(Commands.literal("escolher").then(Commands.argument("opcao", StringArgumentType.word())
                .executes(ctx -> select(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx,"opcao")))))
            .then(Commands.literal("aceitar").executes(ctx -> accept(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("entregar").executes(ctx -> deliver(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("contratos").executes(ctx -> contracts(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("contrato").then(Commands.argument("codigo", StringArgumentType.word())
                .executes(ctx -> contract(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx,"codigo")))))
            .then(Commands.literal("decidir").then(Commands.argument("opcao", StringArgumentType.word())
                .executes(ctx -> decide(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx,"opcao")))))
            .then(Commands.literal("criatura").then(Commands.argument("id", IntegerArgumentType.integer(0))
                .executes(ctx -> inspectMob(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx,"id")))))
            .then(Commands.literal("masmorra").executes(ctx -> dungeonInfo(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("confirmar").executes(ctx -> Dungeon.create(ctx.getSource().getPlayerOrException()))))
            .then(Commands.literal("entrar").executes(ctx -> Dungeon.enter(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("sair").executes(ctx -> Dungeon.leave(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("diarioitem").executes(ctx -> {ServerPlayer p=ctx.getSource().getPlayerOrException(); p.getInventory().add(new ItemStack(DIARIO)); return 1;}))
        );
    }

    public static int begin(ServerPlayer p, int entityId) {
        Entity entity = p.level().getEntity(entityId);
        if (!(entity instanceof Villager v) || p.distanceToSqr(v) > 64 || !v.isAlive()) {
            tell(p, "§cVocê precisa estar perto de um morador vivo."); return 0;
        }
        Progress.State s = Progress.get(p);
        s.dialogNpcId = entityId;
        s.dialogNpcUuid = v.getUUID().toString();
        s.dialogRole = roleOf(v);
        s.dialogNode = "inicio";
        s.dialogExpiresAt = p.level().getGameTime() + 1200;
        int relation = s.relations.getOrDefault(s.dialogNpcUuid, 0);
        tell(p, "§6" + nameOf(v) + " §7(afinidade " + relation + ", honra " + s.honor + ")");
        if (relation <= -35) tell(p, "§cEste morador ainda se lembra do que você fez.");
        return 1;
    }
    private static Villager activeNpc(ServerPlayer p) {
        Progress.State s = Progress.get(p);
        if (s.dialogNpcId < 0 || p.level().getGameTime() > s.dialogExpiresAt) return null;
        Entity e = p.level().getEntity(s.dialogNpcId);
        if (e instanceof Villager v && v.isAlive() && v.getUUID().toString().equals(s.dialogNpcUuid)
                && p.distanceToSqr(v) <= 64) return v;
        return null;
    }
    private static int select(ServerPlayer p, String code) {
        Villager npc = activeNpc(p);
        if (npc == null) { tell(p, "§cConversa encerrada: volte a se aproximar do morador."); return 0; }
        Progress.State s = Progress.get(p);
        DialogueData.Option option = DialogueData.choose(s.dialogRole, s.dialogNode, code);
        if (option == null) { tell(p, "§cResposta indisponível nesta etapa da conversa."); return 0; }
        int old = s.relations.getOrDefault(s.dialogNpcUuid, 0);
        String flag = "dlg:" + s.dialogNpcUuid + ":" + option.effect();
        long now = p.level().getGameTime();
        switch (option.effect()) {
            case "quest" -> { if (s.active) deliver(p); else accept(p); }
            case "contracts" -> contracts(p);
            case "donate" -> {
                if (countItem(p, Items.BREAD) < 2) { tell(p,"§eVocê precisa ter dois pães."); return 0; }
                if (now - s.contractsCompletedAt.getOrDefault(flag, -999999L) < 24000L) {
                    tell(p,"§eVocê já ajudou este morador hoje."); return 0;
                }
                takeItem(p, Items.BREAD, 2);
                s.contractsCompletedAt.put(flag, now);
                s.relations.put(s.dialogNpcUuid, AdventureRules.clampRelation(old + 15));
                changeHonor(p, 7, "Seu gesto alimentou famílias na Fronteira.");
            }
            case "help" -> {
                if (s.honor < -45 || old < -20) {
                    tell(p,"§c'Não vou alimentar alguém em quem não confio.'"); return 0;
                }
                if (now - s.contractsCompletedAt.getOrDefault(flag, -999999L) < 24000L) {
                    tell(p,"§eEste morador não tem mais provisões hoje."); return 0;
                }
                p.getInventory().add(new ItemStack(Items.BREAD, 2));
                s.contractsCompletedAt.put(flag, now);
                tell(p,"§aRecebeu dois pães do morador.");
            }
            case "threat" -> {
                if (now - s.contractsCompletedAt.getOrDefault(flag,-999999L) < 24000L) {
                    tell(p,"§c'Já disse tudo que sei!'"); return 0;
                }
                s.contractsCompletedAt.put(flag, now);
                s.relations.put(s.dialogNpcUuid, AdventureRules.clampRelation(old - 25));
                changeHonor(p, -12, "Ameaçar moradores espalhou medo na aldeia.");
            }
            case "protect", "betray" -> {
                if (s.decisions.containsKey(flag)) {
                    tell(p,"§7Este morador lembra da sua escolha e não mudará de opinião tão facilmente.");
                } else {
                    boolean good = option.effect().equals("protect");
                    s.decisions.put(flag, code);
                    s.relations.put(s.dialogNpcUuid, AdventureRules.clampRelation(old + (good ? 8 : -18)));
                    changeHonor(p, good ? 6 : -14, good ? "Você prometeu proteger o vale." : "Você considerou vender a aldeia ao Eclipse.");
                    if (!good) p.getInventory().add(new ItemStack(Items.EMERALD, 2));
                }
            }
            default -> { }
        }
        s.dialogNode = option.next();
        s.dialogExpiresAt = now + 1200;
        Progress.save(p);
        if (s.dialogNode.equals("fim")) s.dialogNpcId = -1;
        return 1;
    }

    private static int journal(ServerPlayer p) {
        Progress.State s = Progress.get(p);
        tell(p, "§6§lCRÔNICAS DA FRONTEIRA §7• §fNível " + s.level + " XP " + s.xp + "/" + Progress.nextXp(s.level));
        tell(p, "§6História §f" + s.completed + "/" + AdventureRules.STORY.size() + " §7| §eHonra " + s.honor + " (" + AdventureRules.honorTitle(s.honor) + ")");
        if (s.completed < AdventureRules.STORY.size()) {
            AdventureRules.Quest q = AdventureRules.STORY.get(s.completed);
            tell(p, "§6Capítulo " + (s.completed+1) + ": §f" + q.title());
            tell(p, "§7" + q.intro());
            tell(p, "§e" + q.summary() + " §7[" + questProgress(p,q,s.kills) + "/" + q.quantity() + "]");
            tell(p, "§7Responsável: " + DialogueData.profession(q.role()) + " • " + (s.active ? "§aEm andamento" : "§eNão iniciada"));
        } else tell(p,"§aHistória concluída: " + s.ending);
        if (!s.contractId.isEmpty()) {
            AdventureRules.Quest q = side(s.contractId);
            if (q != null) tell(p,"§bContrato: " + q.title() + " §7" + questProgress(p,q,s.contractKills) + "/" + q.quantity());
        }
        tell(p,"§7Comandos: /cronicas honra • /cronicas contratos • /cronicas masmorra • /cronicas decidir");
        return 1;
    }
    private static int honor(ServerPlayer p) {
        Progress.State s = Progress.get(p);
        tell(p,"§eHonra " + s.honor + "/100 §7— " + AdventureRules.honorTitle(s.honor));
        tell(p,"§7Relações lembradas: " + s.relations.size() + " NPCs • Decisões marcantes: " + s.decisions.size());
        s.decisions.forEach((key,value) -> {if (!key.startsWith("dlg:")) tell(p,"§7• " + key + ": " + value);});
        return 1;
    }
    public static void changeHonor(ServerPlayer p,int delta,String reason) {
        Progress.State s=Progress.get(p);
        int old=s.honor;
        s.honor=AdventureRules.clampHonor(old+delta);
        if(old!=s.honor) tell(p,(delta>0?"§a":"§c")+"[Honra " + (delta>0?"+":"") + (s.honor-old) + "] §7"+reason);
        Progress.save(p);
    }
    private static int accept(ServerPlayer p) {
        Progress.State s=Progress.get(p);
        if(s.completed>=AdventureRules.STORY.size()) {tell(p,"§aCampanha já encerrada.");return 0;}
        if(s.active) {tell(p,"§eVocê já está em uma missão.");return 0;}
        AdventureRules.Quest q=AdventureRules.STORY.get(s.completed);
        if(!nearRole(p,q.role())) {tell(p,"§cConverse com um " + DialogueData.profession(q.role()) + " a menos de 8 blocos.");return 0;}
        s.active=true; s.kills=0; s.chapterDecision="";
        tell(p,"§aNova missão: " + q.title() + " §7— " + q.summary());
        Progress.save(p);return 1;
    }
    private static int deliver(ServerPlayer p) {
        Progress.State s=Progress.get(p);
        if(!s.active || s.completed>=AdventureRules.STORY.size()) {tell(p,"§eNão há missão ativa.");return 0;}
        AdventureRules.Quest q=AdventureRules.STORY.get(s.completed);
        if(!nearRole(p,q.role())) {tell(p,"§cVolte ao responsável: " + DialogueData.profession(q.role()));return 0;}
        if(questProgress(p,q,s.kills)<q.quantity()) {tell(p,"§eObjetivo incompleto: " + questProgress(p,q,s.kills) + "/"+q.quantity());return 0;}
        if(AdventureRules.moralDecisionRequired(s.completed) && s.chapterDecision.isEmpty()) {
            tell(p,"§dUma decisão importante define o futuro da Fronteira:");
            if(s.completed==4) tell(p,"§7/cronicas decidir libertar §8OU §7/cronicas decidir vender");
            if(s.completed==7) tell(p,"§7/cronicas decidir devolver §8OU §7/cronicas decidir roubar");
            if(s.completed==11) tell(p,"§7/cronicas decidir selar §8OU §7/cronicas decidir dominar");
            return 1;
        }
        if(q.goal()==AdventureRules.Goal.ITEM) takeItem(p, item(q.target()),q.quantity());
        s.active=false; s.kills=0; s.completed++;
        changeHonor(p,q.honor(),"Você ajudou na missão '" + q.title() + "'.");
        reward(p,q.xp(),q.emeralds());
        tell(p,"§aCapítulo encerrado: §f"+q.title());
        if(s.completed==AdventureRules.STORY.size()) {
            s.ending=AdventureRules.ending(s.honor,s.decisions.getOrDefault("eclipse","selar"));
            tell(p,"§6FINAL: "+s.ending);
        } else tell(p,"§6Próxima missão: "+AdventureRules.STORY.get(s.completed).title());
        Progress.save(p);return 1;
    }
    private static int decide(ServerPlayer p,String choice) {
        Progress.State s=Progress.get(p);
        if(!s.active || !AdventureRules.moralDecisionRequired(s.completed)) {tell(p,"§cNão há escolha moral pendente.");return 0;}
        AdventureRules.Quest q=AdventureRules.STORY.get(s.completed);
        if(questProgress(p,q,s.kills)<q.quantity() || !nearRole(p,q.role())) {tell(p,"§cConclua o objetivo e volte ao responsável.");return 0;}
        int delta=switch(s.completed) {
            case 4 -> choice.equals("libertar")?20:choice.equals("vender")?-25:0;
            case 7 -> choice.equals("devolver")?22:choice.equals("roubar")?-28:0;
            case 11 -> choice.equals("selar")?30:choice.equals("dominar")?-40:0;
            default -> 0;
        };
        if(delta==0 || !s.chapterDecision.isEmpty()) {tell(p,"§cDecisão inválida ou já tomada.");return 0;}
        s.chapterDecision=choice;
        s.decisions.put(q.id(),choice);
        changeHonor(p,delta,"Sua escolha ficará registrada: "+choice);
        if(choice.equals("vender")) p.getInventory().add(new ItemStack(Items.EMERALD,8));
        if(choice.equals("roubar")) p.getInventory().add(new ItemStack(Items.GOLD_INGOT,4));
        if(choice.equals("dominar")) p.getInventory().add(new ItemStack(Items.DIAMOND,5));
        Progress.save(p);return deliver(p);
    }
    private static int contracts(ServerPlayer p) {
        Progress.State s=Progress.get(p);
        tell(p,"§b§lCONTRATOS PARALELOS ("+AdventureRules.SIDE.size()+")");
        if(!s.contractId.isEmpty()) tell(p,"§eAtivo: "+s.contractId+" §7(/cronicas contrato entregar)");
        for(AdventureRules.Quest q:AdventureRules.SIDE)
            if(q.honor()>=0 || s.honor <= -10)
                tell(p,"§b"+q.id()+" §7— "+q.title()+" ("+DialogueData.profession(q.role())+") • /cronicas contrato "+q.id());
        return 1;
    }
    private static AdventureRules.Quest side(String code) {
        return AdventureRules.SIDE.stream().filter(q->q.id().equals(code)).findFirst().orElse(null);
    }
    private static int contract(ServerPlayer p,String code) {
        Progress.State s=Progress.get(p);
        if(code.equals("entregar")) {
            AdventureRules.Quest q=side(s.contractId);
            if(q==null) {tell(p,"§cSem contrato ativo.");return 0;}
            if(!nearRole(p,q.role()) || questProgress(p,q,s.contractKills)<q.quantity()) {
                tell(p,"§eObjetivo ou responsável ainda pendente.");return 0;}
            if(q.goal()==AdventureRules.Goal.ITEM) takeItem(p,item(q.target()),q.quantity());
            s.contractId=""; s.contractKills=0;
            s.contractsCompletedAt.put(q.id(),p.level().getGameTime());
            changeHonor(p,q.honor(),"Contrato concluído: "+q.title());
            reward(p,q.xp(),q.emeralds());
            Progress.save(p);
            tell(p,"§aContrato entregue: "+q.title());
            return 1;
        }
        AdventureRules.Quest q=side(code);
        if(q==null) {tell(p,"§cContrato inexistente.");return 0;}
        if(!s.contractId.isEmpty()) {tell(p,"§eVocê já tem um contrato.");return 0;}
        if(q.honor()<0 && s.honor>-10) {tell(p,"§cOs agentes sombrios não confiam em você.");return 0;}
        if(!nearRole(p,q.role())) {tell(p,"§cProcure um "+DialogueData.profession(q.role())+" a menos de 8 blocos.");return 0;}
        if(p.level().getGameTime()-s.contractsCompletedAt.getOrDefault(code,-999999L)<24000) {
            tell(p,"§eEste contrato só pode ser repetido após 1 dia do jogo.");return 0;
        }
        s.contractId=code; s.contractKills=0;
        Progress.save(p);tell(p,"§bContrato aceito: "+q.title()+" — "+q.summary());return 1;
    }
    private static int inspectMob(ServerPlayer p,int entityId) {
        Entity e=p.level().getEntity(entityId);
        if(!(e instanceof LivingEntity living) || e instanceof Villager || e instanceof ServerPlayer || p.distanceToSqr(e)>64) {
            tell(p,"§cA criatura não está perto.");return 0;}
        Progress.State s=Progress.get(p);
        String kind=e.getType().getDescription().getString();
        String temperament=AdventureRules.mobTemperament(kind,e.getUUID().getLeastSignificantBits());
        String key="mob:"+e.getUUID();
        tell(p,"§d[Percepção] " + kind + " §7— "+temperament);
        if(e instanceof Wolf || e instanceof Cat || e instanceof IronGolem) {
            tell(p,"§7Sua reputação importa. Vínculo individual: " + s.relations.getOrDefault(key,0));
            Item gift=e instanceof Wolf?Items.BONE:e instanceof Cat?Items.COD:Items.IRON_INGOT;
            String flag="mobgift:"+e.getUUID();
            if(countItem(p,gift)>0 && p.level().getGameTime()-s.contractsCompletedAt.getOrDefault(flag,-999999L)>=2400) {
                takeItem(p,gift,1); living.heal(2.0F);
                s.contractsCompletedAt.put(flag,p.level().getGameTime());
                s.relations.merge(key,10,(a,b)->AdventureRules.clampRelation(a+b));
                changeHonor(p,2,"Você cuidou de uma criatura.");
                tell(p,"§aA criatura aceitou um presente: "+gift.getName(new ItemStack(gift)).getString());
            } else tell(p,"§7Ofereça um osso, peixe ou ferro (conforme a criatura) para criar confiança.");
        } else if(e.getType() == EntityTypes.PILLAGER) tell(p,"§cSaqueadores lembram da sua reputação sombria. Desconfie deles.");
        else if(e instanceof Monster) tell(p,"§cEste inimigo não aceita negociação pacífica.");
        else tell(p,"§7Criatura observadora: ataques e interações influenciam a história do mundo.");
        Progress.save(p);return 1;
    }
    private static int dungeonInfo(ServerPlayer p) {
        Progress.State s=Progress.get(p);
        if(!s.active || (s.completed!=4 && s.completed!=11)) {tell(p,"§eMasmorras abrem nos capítulos 5 e 12.");return 0;}
        tell(p,"§cATENÇÃO: escava uma área de 21 x 25 x 8 blocos próximo a você. Faça backup!");
        tell(p,"§eApós escolher um terreno vazio: /cronicas masmorra confirmar");return 1;
    }
    public static int questProgress(ServerPlayer p,AdventureRules.Quest q,int kills) {
        return q.goal()==AdventureRules.Goal.ITEM?countItem(p,item(q.target())):kills;
    }
    private static Item item(String id) {return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));}
    public static int countItem(ServerPlayer p,Item item) {
        int n=0;
        for(ItemStack stack:p.getInventory().getNonEquipmentItems()) if(stack.is(item)) n+=stack.getCount();
        return n;
    }
    public static void takeItem(ServerPlayer p,Item item,int quantity) {
        for(ItemStack stack:p.getInventory().getNonEquipmentItems()) {
            if(stack.is(item) && quantity>0) {int n=Math.min(stack.getCount(),quantity);stack.shrink(n);quantity-=n;}
        }
        p.inventoryMenu.broadcastChanges();
    }
    private static void reward(ServerPlayer p,int xp,int gems) {
        Progress.State s=Progress.get(p);s.xp+=xp;
        while(s.xp>=Progress.nextXp(s.level)) {s.xp-=Progress.nextXp(s.level);s.level++;p.giveExperienceLevels(1);tell(p,"§bNovo nível de aventureiro: "+s.level);}
        if(gems>0)p.getInventory().add(new ItemStack(Items.EMERALD,gems));
        tell(p,"§aRecompensa: "+xp+" XP de RPG e "+gems+" esmeraldas.");
    }
    private static boolean nearRole(ServerPlayer p,String role) {
        for(Villager v:p.level().getEntitiesOfClass(Villager.class,p.getBoundingBox().inflate(8)))
            if(roleOf(v).equals(role)) return true;
        return false;
    }
    public static String roleOf(Villager v) {
        var prof=v.getVillagerData().profession();
        if(prof.is(VillagerProfession.FARMER))return "farmer";
        if(prof.is(VillagerProfession.CLERIC))return "cleric";
        if(prof.is(VillagerProfession.LIBRARIAN))return "librarian";
        if(prof.is(VillagerProfession.WEAPONSMITH))return "weaponsmith";
        if(prof.is(VillagerProfession.ARMORER))return "armorer";
        return "other";
    }
    public static String nameOf(Villager v) {
        return v.hasCustomName() ? Objects.requireNonNull(v.getCustomName()).getString()
            : DialogueData.npcName(roleOf(v),v.getUUID().getLeastSignificantBits()) + ", " + DialogueData.profession(roleOf(v));
    }
    public static void tell(ServerPlayer p,String message) {p.sendSystemMessage(Component.literal(message));}
    public static class JournalItem extends Item {
        public JournalItem(Properties properties) {super(properties);}
        @Override public InteractionResult use(Level level,net.minecraft.world.entity.player.Player player,InteractionHand hand) {
            if(!level.isClientSide() && player instanceof ServerPlayer p) journal(p);
            return InteractionResult.SUCCESS;
        }
    }
}
