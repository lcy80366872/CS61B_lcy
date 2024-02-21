package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validArgs(args,1);
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validArgs(args,2);
                Repository.add(args[1]);
                break;
            case "commit":
                // TODO: handle the `add [filename]` command
                validArgs(args,2);
                Repository.commit(args[1]);
                break;
        }
    }
    private static void validArgs(String[] args, int num) {
        if (args.length != num) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
