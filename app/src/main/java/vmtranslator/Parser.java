package vmtranslator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    // parse each VM command into it's lexical elements

    BufferedReader br;
    String currCommand;

    Parser(File file) throws FileNotFoundException, IOException {
        this.br = new BufferedReader(new FileReader(file));
        this.currCommand = null;
    }

    boolean hasMoreCommands() throws IOException {
        return this.br.ready();
    }

    void advance() throws IOException {
        if (this.hasMoreCommands()) {
            this.currCommand = this.br.readLine().trim().split("//")[0];
            if (this.currCommand.equals("") || this.currCommand.startsWith("//")) {
                this.advance();
            }
        } else {
            this.currCommand = null;
        }
    }

    CommandType commandType() throws InvalidCommandException, NullPointerException {
        String command = this.currCommand.split(" ")[0];
        if (this.isCommandArithmetic(command)) {
            return CommandType.C_ARITHMETIC;
        } else if (this.isCommandMemoryAccess(command)) {
            return CommandType.valueOf("C_" + command.toUpperCase());
        } else {
            throw new InvalidCommandException("Not a valid VM command: " + command);
        }
    }

    private boolean isCommandArithmetic(String command) {
        return command.equals("add") || command.equals("sub") ||
                command.equals("neg") || command.equals("eq") || command.equals("gt") || command.equals("lt")
                || command.equals("and") || command.equals("or") || command.equals("not");
    }

    private boolean isCommandMemoryAccess(String command) {
        return command.equals("pop") || command.equals("push");
    }

    String arg1() throws InvalidCommandException {
        // returns first argument of curr command
        // returns the command itself for C_ARITHMETIC
        // should not be called for C_RETURN
        CommandType cmt;
        try {
            cmt = this.commandType();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        if (cmt.equals(CommandType.C_ARITHMETIC)) {
            return this.currCommand;
        } else if (cmt.equals(CommandType.C_PUSH) || cmt.equals(CommandType.C_POP)) {
            return this.currCommand.split(" ")[1];
        } else {
            throw new InvalidCommandException("arg1 not supported for this command type: " + cmt);
        }
    }

    int arg2() throws InvalidCommandException {
        // returns the second argument of command
        // valid only for C_PUSH, C_POP, C_FUNCTION, C_CALL
        CommandType cmt = this.commandType();
        if (cmt.equals(CommandType.C_POP)
                || cmt.equals(CommandType.C_PUSH)
                || cmt.equals(CommandType.C_FUNCTION)
                || cmt.equals(CommandType.C_CALL)) {
            return Integer.parseInt(this.currCommand.split(" ")[2]);
        } else {
            throw new InvalidCommandException("CommandType:" + cmt.toString() + " does not have arg2");
        }

    }

    void close() throws IOException {
        if (this.br != null) {
            this.br.close();
        }
    }
}
