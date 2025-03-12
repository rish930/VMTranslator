package vmtranslator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CodeWriter {
    /*
    * writes the assembly code that implements the parsed command
    */
    BufferedWriter bw;

    HashMap<String, String> segmentSymbol = new HashMap<>();

    CodeWriter(String filename) throws IOException {
        this.bw = new BufferedWriter(new FileWriter(filename));
        this.segmentSymbol.put("local", "LCL");
        this.segmentSymbol.put("argument", "ARG");
        this.segmentSymbol.put("this", "THIS");
        this.segmentSymbol.put("that", "THAT");
        this.segmentSymbol.put("temp", "TEMP");
    }

    void writeArithmetic(String command) throws IOException, InvalidCommandException {
        // add
        // sub
        // neg
        // eq
        // gt
        // lt
        // and 
        // or
        // not
        switch (command) {
            case "add":
                this.writeAdd();
                break;
            case "sub":
                this.writeSub();
                break;
            case "neg":
                this.writeNeg();
                break;
            case "eq":
                this.writeComparator(command);
            case "lt":
                this.writeComparator(command);
            case "gt":
                this.writeComparator(command);
            case "and":
                this.writeAnd();
            case "or":
                this.writeOr();
            case "not":
                this.writeNot();
            default:
                throw new InvalidCommandException("Command not supported for assembly conversion:" + command);
        }
   
        
    }
    private void writeAdd() throws IOException {

        String assembly = 
        """
        // add
        @SP
        M=M-1
        A=M
        D=M
        @SP
        A=M-1
        M=D+M
        """;
        this.bw.write(assembly);
    }

    private void writeSub() throws IOException {

        String assembly = 
        """
        // sub
        @SP
        M=M-1
        A=M
        D=M
        @SP
        A=M-1
        M=M-D
        """;
        this.bw.write(assembly);
        
    }

    private void writeNeg() throws IOException {

        String assembly = 
        """
        // neg
        @SP
        A=M-1
        M=-M
        """;
        this.bw.write(assembly);
        
    }

    private void writeComparator(String command) throws IOException {
        String assembly = 
        """
        // %s
        @SP
        M=M-1
        A=M
        D=M
        A=A-1
        D=D-M
        @SP
        M=M-1
        @TRUE
        D;%s
        (FALSE)
        @SP
        A=M
        M=0
        @END
        0;JMP
        (TRUE)
        @SP
        A=M
        M=-1
        @END
        0;JMP
        (END)
        @SP
        M=M+1
        (STOP)
        @STOP
        0;JMP
        """;
        HashMap<String, String> cjump = new HashMap<>();
        cjump.put("eq", "JEQ");
        cjump.put("lt", "JLT");
        cjump.put("gt", "JGT");

        assembly = String.format(assembly, command, cjump.get(command));
        this.bw.write(assembly);
    }

    private void writeAnd() throws IOException {
        String assembly = 
        """
        // and
        @SP
        M=M-1
        A=M
        D=M
        A=A-1
        M=D&M
        """;
        this.bw.write(assembly);
    }

    private void writeOr() throws IOException {
        String assembly = 
        """
        // or
        @SP
        M=M-1
        A=M
        D=M
        A=A-1
        M=D|M
        """;
        this.bw.write(assembly);
    }

    private void writeNot() throws IOException {
        String assembly = 
        """
        // not
        @SP
        A=M-1
        M=!M
        """;
        this.bw.write(assembly);
    }

    void writePushPop(String command, String segment, int index) throws IOException, InvalidCommandException {
        /*
        writes to the output file assembly code
        that implements the given command
        is either C_PUSH or C_POP
        segment = local/argument/this/that
        push segment i
        take value from local memory segment
        push the value to stack
        */
        
        String topComment = String.join(" ", command, segment, Integer.toString(index));
        if(command.equals("push")) {
            if (segment.equals("local")
                || segment.equals("argument")
                || segment.equals("this")
                || segment.equals("that")) {
                    this.writePush1(topComment, segmentSymbol.get(segment), index);
                }
            else if (segment.equals("temp")) {
                this.writePushTemp(topComment, index);
            } 
            else if (segment.equals("constant")) {
                this.writePushConstant(topComment, index);
            } else {
                throw new InvalidCommandException("Argument not supported to convert to assembly");
            }
        } else if (command.equals("pop")) {
            if (segment.equals("local")
                || segment.equals("argument")
                || segment.equals("this")
                || segment.equals("that")) {
                    this.writePop1(topComment, segmentSymbol.get(segment), index);
                }
            else if (segment.equals("temp")) {
                this.writePopTemp(topComment, index);
            } else {
                throw new InvalidCommandException("Argument not supported to convert to assembly");
            }
        }
    }

    private void writePush1(String topComment,  String segmentSymbol, int index) throws IOException {
        String assembly = 
        """
        // %s 
        @%d
        D=A
        @%s
        A=D+M
        D=M
        @SP
        A=M
        M=D
        @SP
        M=M+1
        """;
        assembly = String.format(assembly, topComment, index, segmentSymbol);
        this.bw.write(assembly);
    }

    private void writePushTemp(String topComment, int index) throws IOException {
        String assembly = 
        """
        // %s
        @%d
        D=M
        @SP
        A=M
        M=D
        @SP
        M=M+1
        """;
        assembly = String.format(assembly, topComment, 5+index);
        this.bw.write(assembly);
        
        
    }

    private void writePushConstant(String topComment,  int index) throws IOException {
        String assembly = 
        """
        // %s 
        @%d
        D=A
        @SP
        A=M
        M=D
        @SP
        M=M+1
        """;
        assembly = String.format(assembly, topComment, index);
        this.bw.write(assembly);
        
        
    }
    
    private void writePop1(String topComment,  String segmentSymbol, int index) throws IOException {
        String assembly = 
        """
        // %s
        @%d
        D=A
        @%s
        D=D+M
        @address
        M=D
        @SP
        M=M-1
        A=M
        D=M
        @address 
        A=M
        M=D
        """;
        assembly = String.format(assembly, topComment, index, segmentSymbol);
        this.bw.write(assembly);
           
    }

    private void writePopTemp(String topComment, int index) throws IOException {
        String assembly = 
        """
        // %s
        @SP
        M=M-1
        A=M
        D=M
        @%d
        M=D
        """;
        assembly = String.format(assembly, topComment, 5+index);
        this.bw.write(assembly);
        
    }
    
    void close() throws IOException {
        this.bw.close();
    }
}
