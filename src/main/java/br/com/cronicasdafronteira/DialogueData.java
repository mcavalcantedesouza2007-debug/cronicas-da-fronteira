package br.com.cronicasdafronteira;

import java.util.List;
import java.util.Map;

/** Árvore determinística compartilhada entre client e servidor; lógica pura e testável. */
public final class DialogueData {
    private DialogueData() {}
    public record Option(String id, String caption, String next, String effect) {}
    public record Node(String id, String line, List<Option> options) {}
    public static Option opt(String id, String text, String next, String effect) {
        return new Option(id, text, next, effect);
    }

    public static String npcRole(String raw) {
        if (raw.contains("farmer")) return "farmer";
        if (raw.contains("cleric")) return "cleric";
        if (raw.contains("librarian")) return "librarian";
        if (raw.contains("weaponsmith")) return "weaponsmith";
        if (raw.contains("armorer")) return "armorer";
        return "other";
    }
    public static String profession(String role) {
        return switch (role) {
            case "farmer" -> "Fazendeiro";
            case "cleric" -> "Clérigo";
            case "librarian" -> "Bibliotecário";
            case "weaponsmith" -> "Armeiro";
            case "armorer" -> "Ferreiro";
            default -> "Viajante";
        };
    }
    private static final Map<String, String[]> NAMES = Map.of(
        "farmer", new String[]{"Eldric", "Marwen", "Tessara", "Borin"},
        "cleric", new String[]{"Iria", "Selyne", "Orwen", "Althea"},
        "librarian", new String[]{"Bram", "Adriel", "Isemar", "Nerys"},
        "weaponsmith", new String[]{"Doran", "Maelis", "Rurik", "Kaelen"},
        "armorer", new String[]{"Kael", "Dagna", "Torren", "Veyra"},
        "other", new String[]{"Maera", "Nolan", "Silas", "Ysolda"}
    );
    public static String npcName(String role, long uniqueBits) {
        String[] names = NAMES.getOrDefault(role, NAMES.get("other"));
        return names[Math.floorMod((int)(uniqueBits ^ (uniqueBits >>> 32)), names.length)];
    }
    public static String greeting(String role) {
        return switch (role) {
            case "farmer" -> "A fome cresce no vale. Uma mão amiga vale uma safra inteira.";
            case "cleric" -> "Os sinos tocam antes do amanhecer. Os mortos não estão em paz.";
            case "librarian" -> "Os livros esquecem menos que os homens. E há algo terrível nas páginas.";
            case "weaponsmith" -> "Há ferrugem nas espadas e medo nos corações. Precisamos de coragem.";
            case "armorer" -> "Consertar uma armadura é fácil; reparar uma promessa, não.";
            default -> "Aqui toda palavra tem preço, aventureiro. E nenhuma é de graça.";
        };
    }
    public static String rumor(String role) {
        return switch (role) {
            case "farmer" -> "Vi cinzas negras na plantação. Os cavalos se recusaram a cruzar a estrada.";
            case "cleric" -> "A Dama do Eclipse fez uma aliança antiga. Algumas almas ainda podem ser salvas.";
            case "librarian" -> "O Carcereiro Cinzento serviu à Dama. A prisão ainda guarda nomes apagados.";
            case "weaponsmith" -> "A guarda desapareceu na ponte. Um traidor indicou a passagem aos saqueadores.";
            case "armorer" -> "A Dama quer que escolhamos entre segurança e liberdade. Não confie no preço.";
            default -> "Há ouro para quem esquece o que viu. Há silêncio para quem aceita.";
        };
    }
    public static Node node(String role, String id) {
        return switch (id) {
            case "inicio" -> new Node("inicio", greeting(role), List.of(
                opt("rumores", "O que está acontecendo no vale?", "rumores", "none"),
                opt("missao", "Precisa de ajuda com alguma missão?", "inicio", "quest"),
                opt("favor", "Posso pedir um favor?", "favor", "none"),
                opt("escolha", "Há algo que você está escondendo?", "escolha", "none"),
                opt("despedir", "Preciso seguir viagem.", "fim", "none")));
            case "rumores" -> new Node("rumores", rumor(role), List.of(
                opt("investigar", "Conte mais sobre essa ameaça.", "investigar", "none"),
                opt("contratos", "Existe algum contrato de caça?", "rumores", "contracts"),
                opt("voltar", "Vamos falar de outra coisa.", "inicio", "none")));
            case "investigar" -> new Node("investigar", "Se o Eclipse despertar, precisaremos de aliados. Você tem de decidir quem merece sua confiança.", List.of(
                opt("proteger", "Vou proteger os inocentes.", "resultado_bom", "protect"),
                opt("trair", "Talvez eu possa negociar com o Eclipse.", "resultado_ruim", "betray"),
                opt("voltar", "Ainda preciso pensar.", "inicio", "none")));
            case "favor" -> new Node("favor", "A confiança é uma ponte que construímos em pequenos gestos.", List.of(
                opt("doar", "Oferecer dois pães à aldeia.", "resultado_bom", "donate"),
                opt("pedir", "Pedir um pouco de comida.", "resultado_ajuda", "help"),
                opt("ameacar", "Intimidar para conseguir ajuda.", "resultado_ruim", "threat"),
                opt("voltar", "Deixe para outra hora.", "inicio", "none")));
            case "escolha" -> new Node("escolha", "Um mensageiro do Eclipse procura informantes. Você revelaria nosso esconderijo por ouro?", List.of(
                opt("silencio", "Jamais venderia a aldeia.", "resultado_bom", "protect"),
                opt("ouro", "Depende de quanto pagarem.", "resultado_ruim", "betray"),
                opt("voltar", "Isso não me diz respeito.", "inicio", "none")));
            case "resultado_bom" -> new Node("resultado_bom", "Talvez ainda exista esperança na Fronteira. Não esquecerei sua resposta.", List.of(
                opt("voltar", "Continuar conversando.", "inicio", "none"),
                opt("despedir", "Até breve.", "fim", "none")));
            case "resultado_ruim" -> new Node("resultado_ruim", "Você fala como os mercenários... Guardarei suas palavras.", List.of(
                opt("voltar", "Continuar conversando.", "inicio", "none"),
                opt("despedir", "Ir embora.", "fim", "none")));
            case "resultado_ajuda" -> new Node("resultado_ajuda", "A aldeia não tem muito, mas ajudamos quem ajuda a Fronteira.", List.of(
                opt("voltar", "Continuar conversando.", "inicio", "none"),
                opt("despedir", "Obrigado. Até mais.", "fim", "none")));
            default -> new Node("fim", "Que os caminhos sejam gentis com você.", List.of());
        };
    }
    public static Option choose(String role, String node, String option) {
        return node(role, node).options().stream().filter(o -> o.id().equals(option)).findFirst().orElse(null);
    }
}
