import br.com.cronicasdafronteira.DialogueData;
import java.util.*;

/** Testa a árvore e escolhas de cada papel, sem iniciar Minecraft. */
public class TestDialogueData {
    private static int checks = 0;
    private static void check(boolean ok, String name) {
        checks++;
        if (!ok) throw new AssertionError(name);
    }
    public static void main(String[] args) {
        String[] roles = {"farmer", "cleric", "librarian", "weaponsmith", "armorer", "other"};
        for (String role : roles) {
            check(!DialogueData.greeting(role).isBlank(), "saudação " + role);
            check(!DialogueData.rumor(role).isBlank(), "rumor " + role);
            check(!DialogueData.profession(role).isBlank(), "profissão " + role);
            check(DialogueData.npcName(role, 42).equals(DialogueData.npcName(role, 42)), "nome estável " + role);
            check(DialogueData.choose(role, "inicio", "rumores") != null, "rumor inicial " + role);
            check(DialogueData.choose(role, "inicio", "nao_existe") == null, "resposta inválida " + role);
            Set<String> visited = new HashSet<>();
            Deque<String> frontier = new ArrayDeque<>();
            frontier.add("inicio");
            while (!frontier.isEmpty()) {
                String nodeId = frontier.removeFirst();
                if (!visited.add(nodeId)) continue;
                DialogueData.Node node = DialogueData.node(role, nodeId);
                check(!node.line().isBlank(), "fala " + role + "/" + nodeId);
                Set<String> opts = new HashSet<>();
                for (DialogueData.Option option : node.options()) {
                    check(opts.add(option.id()), "opção única " + role + "/" + nodeId + "/" + option.id());
                    check(DialogueData.choose(role, nodeId, option.id()) != null, "opção navegável");
                    check(!DialogueData.node(role, option.next()).line().isBlank(), "destino válido");
                    frontier.add(option.next());
                }
            }
            check(visited.containsAll(Set.of("inicio","rumores","investigar","favor","escolha","resultado_bom","resultado_ruim","resultado_ajuda","fim")), "nós acessíveis " + role);
            check("protect".equals(DialogueData.choose(role,"investigar","proteger").effect()), "decisão heroica");
            check("betray".equals(DialogueData.choose(role,"investigar","trair").effect()), "decisão sombria");
            check("donate".equals(DialogueData.choose(role,"favor","doar").effect()), "doação");
            check("threat".equals(DialogueData.choose(role,"favor","ameacar").effect()), "intimidação");
        }
        System.out.println("APROVADO: " + checks + " verificações de diálogo.");
    }
}
