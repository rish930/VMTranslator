package vmtranslator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CodeWriter {
    /*
    * writes the assembly code that implements the parsed command
    */
    private String filename;
    private BufferedWriter bw;

    private HashMap<String, String> segmentSymbol = new HashMap<>();

    CodeWriter(String filename) throws IOException {
        this.filename = filename;
        this.bw = new BufferedWriter(new FileWriter(this.filename));
        this.segmentSymbol.put("local", "LCL");
        this.segmentSymbol.put("argument", "ARG");
        this.segmentSymbol.put("this", "THIS");
        this.segmentSymbol.put("that", "THAT");
        this.segmentSymbol.put("temp", "TEMP");
    }

    void setFilename(String filename) throws IOException {
        // to start translation of a new file
        // called by VMTranslator
        try {
            this.close();
        } catch (IOException e) {
            System.out.println("Isse while closing buffered writer");
            e.printStackTrace();
        }
        this.filename = filename;
        this.bw = new BufferedWriter(new FileWriter(this.filename));
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
    private void writeAdd() {

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
        this.write(assembly);
    }

    private void writeSub() {

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
        this.write(assembly);
        
    }

    private void writeNeg() {

        String assembly = 
        """
        // neg
        @SP
        A=M-1
        M=-M
        """;
        this.write(assembly);
        
    }

    private void writeComparator(String command) {
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
        this.write(assembly);
    }

    private void writeAnd() {
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
        this.write(assembly);
    }

    private void writeOr() {
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
        this.write(assembly);
    }

    private void writeNot() {
        String assembly = 
        """
        // not
        @SP
        A=M-1
        M=!M
        """;
        this.write(assembly);
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

    private void writePush1(String topComment,  String segmentSymbol, int index) {
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
        this.write(assembly);
    }

    private void writePushTemp(String topComment, int index) {
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
        this.write(assembly);
        
        
    }

    private void writePushConstant(String topComment,  int index) {
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
        this.write(assembly);
        
        
    }
    
    private void writePop1(String topComment,  String segmentSymbol, int index) {
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
        this.write(assembly);
           
    }

    private void writePopTemp(String topComment, int index) {
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
        this.write(assembly);
        
    }
    
    void writeLabel(String label) {
        String assembly = 
        """
        // label
        (%s)
        """;
        assembly = String.format(assembly, label);
        this.write(assembly);

    }

    void writeGoto(String label) {
        String assembly = 
        """
        // goto
        @%s
        0;JMP
        """;
        assembly = String.format(assembly, label);
        this.write(assembly);
    }

    void writeIf(String label) {
        // if true(i.e. -1) on stack then jump to label
        String assembly = 
        """
        // if-goto
        @SP
        M=M-1
        A=M
        D=M
        @%s
        D+1;JMP
        """;
        assembly = String.format(assembly, label);
        this.write(assembly);
    }

    void close() throws IOException {
        this.bw.close();
    }

    private void write(String assembly) {
        try {
            this.bw.write(assembly);
        } catch (IOException e) {
            System.out.println("Could not write to file");
            e.printStackTrace();
        }
    }
}
