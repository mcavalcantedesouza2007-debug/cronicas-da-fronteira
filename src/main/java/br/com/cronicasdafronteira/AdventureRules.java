package br.com.cronicasdafronteira;

import java.util.List;
import java.util.Locale;

/** Regras puras de RPG: não dependem de Minecraft/Fabric e são testáveis com javac puro. */
public final class AdventureRules {
    private AdventureRules() {}
    public enum Goal { ITEM, KILL, BOSS }
    public record Quest(String id, String title, String role, Goal goal, String target,
                        int quantity, int xp, int emeralds, int honor,
                        String intro, String summary) {}

    public static final List<Quest> STORY = List.of(
        new Quest("pao", "O Pão da Esperança", "farmer", Goal.ITEM, "minecraft:wheat", 12, 65, 3, 5,
            "As caravanas não chegam. Os campos secaram e o inverno se aproxima.", "Entregue 12 trigos."),
        new Quest("vigilia", "A Vigília dos Mortos", "cleric", Goal.KILL, "minecraft:zombie", 6, 110, 5, 4,
            "Os mortos marcham em direção às casas. A clériga pede uma vigília.", "Derrote 6 zumbis."),
        new Quest("pergaminho", "O Pergaminho Perdido", "librarian", Goal.ITEM, "minecraft:paper", 16, 100, 5, 3,
            "Bram acredita que um mapa rasgado conduz até uma antiga prisão.", "Entregue 16 papéis."),
        new Quest("guarda", "A Guarda que Não Voltou", "weaponsmith", Goal.KILL, "minecraft:skeleton", 6, 140, 7, 5,
            "A patrulha desapareceu no vale das lápides. Siga as pegadas.", "Derrote 6 esqueletos."),
        new Quest("carcereiro", "A Prisão do Carcereiro Cinzento", "armorer", Goal.BOSS, "guardiao", 1, 250, 14, 0,
            "O Carcereiro mantém o caminho fechado. Enfrente-o na cripta.", "Derrote o Carcereiro Cinzento na masmorra."),
        new Quest("refugiados", "Vozes do Exílio", "farmer", Goal.ITEM, "minecraft:bread", 16, 100, 6, 7,
            "Refugiados famintos fugiram das terras do Eclipse.", "Entregue 16 pães."),
        new Quest("teia", "A Floresta Silenciosa", "cleric", Goal.KILL, "minecraft:spider", 8, 150, 8, 4,
            "Aranhas bloquearam a estrada dos peregrinos. A floresta ficou muda.", "Derrote 8 aranhas."),
        new Quest("caravana", "O Ouro da Caravana", "librarian", Goal.ITEM, "minecraft:gold_ingot", 8, 180, 8, 0,
            "Uma caravana desapareceu levando os tributos da aldeia.", "Reúna 8 barras de ouro para a caravana."),
        new Quest("afogados", "Os Sinos Submersos", "cleric", Goal.KILL, "minecraft:drowned", 8, 160, 9, 4,
            "Os afogados carregam símbolos do culto. A água esconde mensagens.", "Derrote 8 afogados."),
        new Quest("cerco", "O Cerco de Pedra", "weaponsmith", Goal.KILL, "minecraft:pillager", 8, 200, 10, 6,
            "Os saqueadores cercaram as rotas de abastecimento.", "Derrote 8 saqueadores."),
        new Quest("ritual", "As Cinzas do Ritual", "librarian", Goal.ITEM, "minecraft:redstone", 20, 180, 10, 0,
            "O ritual da Dama do Eclipse precisa ser interrompido.", "Reúna 20 pós de redstone."),
        new Quest("eclipse", "A Dama do Eclipse", "armorer", Goal.BOSS, "eclipse", 1, 420, 22, 0,
            "A Dama do Eclipse oferece poder em troca da liberdade da Fronteira.", "Derrote a Dama do Eclipse na masmorra.")
    );

    public static final List<Quest> SIDE = List.of(
        new Quest("fazenda", "Sementes para o Amanhã", "farmer", Goal.ITEM, "minecraft:wheat_seeds", 24, 65, 3, 5, "O solo ainda pode florescer.", "Entregue 24 sementes."),
        new Quest("horta", "A Colheita dos Esquecidos", "farmer", Goal.ITEM, "minecraft:carrot", 20, 80, 4, 5, "Crianças aguardam a sopa da vila.", "Entregue 20 cenouras."),
        new Quest("caminho", "A Estrada dos Lobos", "farmer", Goal.KILL, "minecraft:zombie", 10, 100, 5, 3, "Os mortos interditaram o caminho.", "Elimine 10 zumbis."),
        new Quest("cura", "Ervas para a Clériga", "cleric", Goal.ITEM, "minecraft:glow_berries", 12, 90, 5, 6, "A enfermaria precisa de ervas luminescentes.", "Entregue 12 bagas brilhantes."),
        new Quest("ossos", "Ossos Sem Descanso", "cleric", Goal.KILL, "minecraft:skeleton", 12, 125, 6, 4, "Os espíritos se agitam na necrópole.", "Derrote 12 esqueletos."),
        new Quest("lagrimas", "Páginas Manchadas", "librarian", Goal.ITEM, "minecraft:ink_sac", 10, 80, 4, 3, "Reescrever os registros exige tinta.", "Entregue 10 bolsas de tinta."),
        new Quest("mapas", "Cartógrafos Perdidos", "librarian", Goal.ITEM, "minecraft:map", 2, 120, 7, 4, "Os mapas podem reabrir caminhos de comércio.", "Entregue 2 mapas vazios."),
        new Quest("ferro", "Ferro para a Vigília", "weaponsmith", Goal.ITEM, "minecraft:iron_ingot", 16, 120, 7, 3, "A guarda precisa de ferragens novas.", "Entregue 16 barras de ferro."),
        new Quest("cordas", "Linhas da Resistência", "weaponsmith", Goal.ITEM, "minecraft:string", 20, 80, 4, 3, "Arcos e armadilhas precisam de cordas.", "Entregue 20 linhas."),
        new Quest("arqueiros", "Caça aos Arqueiros", "weaponsmith", Goal.KILL, "minecraft:stray", 6, 150, 7, 4, "Arqueiros gélidos perseguem viajantes.", "Derrote 6 errantes."),
        new Quest("escudos", "Armaduras Desgastadas", "armorer", Goal.ITEM, "minecraft:leather", 24, 100, 5, 3, "Os escudos precisam de reforço.", "Entregue 24 couros."),
        new Quest("carvao", "A Forja Noturna", "armorer", Goal.ITEM, "minecraft:coal", 32, 95, 5, 3, "O fogo da forja não pode morrer.", "Entregue 32 carvões."),
        new Quest("muralhas", "Os Saqueadores da Ponte", "armorer", Goal.KILL, "minecraft:pillager", 10, 160, 8, 5, "A ponte sofreu três ataques.", "Derrote 10 saqueadores."),
        new Quest("culto", "O Preço da Traição", "other", Goal.ITEM, "minecraft:gold_ingot", 12, 150, 12, -12, "Um informante do culto paga por silêncio.", "Entregue 12 barras de ouro ao informante."),
        new Quest("contrabando", "Mercado de Sombras", "other", Goal.ITEM, "minecraft:emerald", 8, 145, 15, -15, "Mercadores compram lealdades com esmeraldas.", "Entregue 8 esmeraldas ao intermediário."),
        new Quest("invasores", "Sangue pela Fronteira", "other", Goal.KILL, "minecraft:vindicator", 6, 180, 10, -7, "A ordem clandestina oferece um pagamento brutal.", "Derrote 6 vindicators para a ordem."),
        new Quest("poeira", "O Sinal do Eclipse", "other", Goal.ITEM, "minecraft:gunpowder", 16, 130, 10, -9, "Explosivos alimentam a guerra do culto.", "Entregue 16 pólvoras."),
        new Quest("madeira", "Casas para os Desabrigados", "farmer", Goal.ITEM, "minecraft:oak_log", 32, 90, 4, 6, "O inverno aproxima-se das ruínas da aldeia.", "Entregue 32 troncos de carvalho.")
    );

    public static int clampHonor(int value) { return Math.max(-100, Math.min(100, value)); }
    public static int clampRelation(int value) { return Math.max(-100, Math.min(100, value)); }
    public static String honorTitle(int value) {
        if (value >= 70) return "Lenda da Fronteira";
        if (value >= 35) return "Protetor do Povo";
        if (value >= 10) return "Bem-visto";
        if (value > -10) return "Desconhecido";
        if (value > -35) return "Suspeito";
        if (value > -70) return "Temido";
        return "Inimigo da Fronteira";
    }
    public static String ending(int honor, String finalChoice) {
        if ("dominar".equals(finalChoice)) return "O TRONO DAS SOMBRAS";
        if (honor >= 35) return "O GUARDIÃO DA FRONTEIRA";
        if (honor <= -35) return "A PAZ PELO MEDO";
        return "A PAZ INCERTA";
    }
    public static boolean moralDecisionRequired(int chapter) { return chapter == 4 || chapter == 7 || chapter == 11; }
    public static String mobTemperament(String mobName, long leastBits) {
        String[] traits = {"cauteloso", "agressivo", "observador", "territorial", "teimoso", "curioso"};
        int ix = Math.floorMod((int)(leastBits ^ (leastBits >>> 32)), traits.length);
        return mobName.toLowerCase(Locale.ROOT) + " " + traits[ix];
    }
}
