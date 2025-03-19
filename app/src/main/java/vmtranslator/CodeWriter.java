package vmtranslator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CodeWriter {
    /*
     * writes the assembly code that implements the parsed command
     */
    private String asmFilename;
    private BufferedWriter bw;
    private String vmfilename;
    private int callCount;
    private String currFunction;

    private HashMap<String, String> segmentSymbol = new HashMap<>();

    CodeWriter(String filename) throws IOException {
        this.asmFilename = filename;
        this.bw = new BufferedWriter(new FileWriter(this.asmFilename));
        this.segmentSymbol.put("local", "LCL");
        this.segmentSymbol.put("argument", "ARG");
        this.segmentSymbol.put("this", "THIS");
        this.segmentSymbol.put("that", "THAT");
        this.segmentSymbol.put("temp", "TEMP");
        this.vmfilename = "";
        this.currFunction = "";
        this.callCount = 0;
    }

    void close() throws IOException {
        this.bw.close();
    }

    void setVmFilename(String filename) throws IOException {
        // informs that translation of new vm file has started
        this.vmfilename = filename;

    }

    void writeInit() {
        this.vmfilename = "Sys";
        this.currFunction = "BootstrapCode";
        String assembly = """
                // setup
                @256
                D=A
                @SP
                M=D
                """;
        this.write(assembly);
        this.writeCall("Sys.init", 0);
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
                break;
            case "lt":
                this.writeComparator(command);
                break;
            case "gt":
                this.writeComparator(command);
                break;
            case "and":
                this.writeAnd();
                break;
            case "or":
                this.writeOr();
                break;
            case "not":
                this.writeNot();
                break;
            default:
                throw new InvalidCommandException("Command not supported for assembly conversion:" + command);
        }

    }

    private void writeAdd() {

        String assembly = """
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

        String assembly = """
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

        String assembly = """
                // neg
                @SP
                A=M-1
                M=-M
                """;
        this.write(assembly);

    }

    private void writeComparator(String command) {
        String assembly = """
                // %s
                @SP
                M=M-1
                A=M
                D=M
                @SP
                M=M-1
                A=M
                D=M-D
                @%s //jump to TRUE
                D;%s // cjump
                (%s) // continue false
                @SP
                A=M
                M=0
                @%s // jump to END
                0;JMP
                (%s) // true branch
                @SP
                A=M
                M=-1
                (%s) // END
                @SP
                M=M+1
                """;
        HashMap<String, String> cjump = new HashMap<>();
        cjump.put("eq", "JEQ");
        cjump.put("lt", "JLT");
        cjump.put("gt", "JGT");

        String prefix = "";
        if (this.currFunction != "") {
            prefix = this.currFunction + "$";
        }
        assembly = String.format(assembly, command, prefix + "TRUE", cjump.get(command), prefix + "FALSE",
                prefix + "END", prefix + "TRUE", prefix + "END");
        this.write(assembly);
    }

    private void writeAnd() {
        String assembly = """
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
        String assembly = """
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
        String assembly = """
                // not
                @SP
                A=M-1
                M=!M
                """;
        this.write(assembly);
    }

    void writePushPop(String command, String segment, int index) throws IOException, InvalidCommandException {
        /*
         * writes to the output file assembly code
         * that implements the given command
         * is either C_PUSH or C_POP
         * segment = local/argument/this/that
         * push segment i
         * take value from local memory segment
         * push the value to stack
         */

        String topComment = String.join(" ", command, segment, Integer.toString(index));
        if (command.equals("push")) {
            if (segment.equals("local")
                    || segment.equals("argument")
                    || segment.equals("this")
                    || segment.equals("that")) {
                this.writePush1(topComment, segmentSymbol.get(segment), index);
            } else if (segment.equals("temp")) {
                this.writePushTemp(topComment, index);
            } else if (segment.equals("constant")) {
                this.writePushConstant(topComment, index);
            } else if (segment.equals("pointer") && index >= 0 && index <= 1) {
                this.writePushPointer(index);
            } else if (segment.equals("static")) {
                this.writePushStatic(index);
            } else {
                throw new InvalidCommandException("Argument not supported to convert to assembly");
            }
        } else if (command.equals("pop")) {
            if (segment.equals("local")
                    || segment.equals("argument")
                    || segment.equals("this")
                    || segment.equals("that")) {
                this.writePop1(topComment, segmentSymbol.get(segment), index);
            } else if (segment.equals("temp")) {
                this.writePopTemp(topComment, index);
            } else if (segment.equals("pointer") && index >= 0 && index <= 1) {
                this.writePopPointer(index);
            } else if (segment.equals("static")) {
                this.writePopStatic(index);
            } else {
                throw new InvalidCommandException("Argument not supported to convert to assembly");
            }
        }
    }

    private void writePush1(String topComment, String segmentSymbol, int index) {
        String assembly = """
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
        String assembly = """
                // %s
                @%d
                D=M
                @SP
                A=M
                M=D
                @SP
                M=M+1
                """;
        assembly = String.format(assembly, topComment, 5 + index);
        this.write(assembly);

    }

    private void writePushConstant(String topComment, int index) {
        String assembly = """
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

    private void writePop1(String topComment, String segmentSymbol, int index) {
        String assembly = """
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
        String assembly = """
                // %s
                @SP
                M=M-1
                A=M
                D=M
                @%d
                M=D
                """;
        assembly = String.format(assembly, topComment, 5 + index);
        this.write(assembly);

    }

    private void writePushPointer(int index) {
        String assembly = """
                // push pointer %d
                @%s // THIS or THAT
                D=M
                @SP
                A=M
                M=D
                @SP
                M=M+1
                """;
        String segment;
        if (index == 0) {
            segment = "THIS";
        } else {
            segment = "THAT";
        }
        assembly = String.format(assembly, index, segment);
        this.write(assembly);
    }

    private void writePopPointer(int index) {
        String assembly = """
                // pop pointer %d
                @SP
                M=M-1
                A=M
                D=M
                @%s // THIS or THAT
                M=D
                """;
        String segment;
        if (index == 0) {
            segment = "THIS";
        } else {
            segment = "THAT";
        }
        assembly = String.format(assembly, index, segment);
        this.write(assembly);
    }

    private void writePushStatic(int index) {
        String assembly = """
                // push static %d
                @%s //xxx.i
                D=M
                @SP
                A=M
                M=D
                @SP
                M=M+1
                """;
        assembly = String.format(assembly, index, this.vmfilename+"."+Integer.toString(index));
        this.write(assembly);
    }

    private void writePopStatic(int index) {
        String assembly = """
                // pop static %d
                @SP
                M=M-1
                A=M
                D=M
                @%s // xxx.i
                M=D
                """;
        assembly = String.format(assembly, index, this.vmfilename+"."+Integer.toString(index));
        this.write(assembly);
    }
    void writeLabel(String label) {
        if (currFunction != "") {
            this.writeLabelOnly(this.currFunction + "$" + label);
        } else {
            this.writeLabelOnly(label);
        }
    }

    void writeLabelOnly(String label) {
        String assembly = """
                // label
                (%s)
                """;
        assembly = String.format(assembly, label);
        this.write(assembly);
    }

    void writeGoto(String label) {
        if (currFunction != "") {
            this.writeGotoOnly(this.currFunction + "$" + label);
        } else {
            this.writeGotoOnly(label);
        }
    }

    private void writeGotoOnly(String label) {
        String assembly = """
                // goto %s
                @%s
                0;JMP
                """;
        assembly = String.format(assembly, label, label);
        this.write(assembly);
    }

    void writeIf(String label) {
        // if true(i.e. -1) on stack then jump to label
        String assembly = """
                // if-goto %s
                @SP
                M=M-1
                A=M
                D=M
                @%s
                D+1;JEQ
                D;JGT
                """;
        if (this.currFunction == "") {
            assembly = String.format(assembly, label, label);
        } else {
            assembly = String.format(assembly, this.currFunction + "$" + label, this.currFunction + "$" + label);
        }
        this.write(assembly);
    }

    void writeFunction(String functionName, int nVars) throws IOException, InvalidCommandException {
        // make a label
        // repeat nVars times to set local variables
        // push constant 0
        this.callCount = 1;
        this.currFunction = functionName;
        this.writeLabelOnly(this.currFunction);
        for (int i = 0; i < nVars; i++) {
            this.writePushPop("push", "constant", 0);
        }
    }

    void writeCall(String functionName, int nArgs) {
        String assembly = "";
        // top comment
        String topComment = "// call " + functionName + " " + Integer.toString(nArgs);
        assembly = assembly + topComment;
        // return address
        String retAddressLabel = this.currFunction + "$ret." + this.callCount;
        String saveReturnAddress = """
                // save returnAddrLabel
                @%s
                D=A
                @SP
                A=M
                M=D
                @SP
                M=M+1
                """;
        assembly = assembly + String.format(saveReturnAddress, retAddressLabel);

        String saveCallerSegment = """
                // save caller memory segment
                @%s
                D=M
                @SP
                A=M
                M=D
                @SP
                M=M+1
                """;

        assembly = assembly + String.format(saveCallerSegment, "LCL");
        assembly = assembly + String.format(saveCallerSegment, "ARG");
        assembly = assembly + String.format(saveCallerSegment, "THIS");
        assembly = assembly + String.format(saveCallerSegment, "THAT");

        String calleeArg = """
                // set callee ARG
                @5
                D=A
                @%d
                D=D+A
                @SP
                D=M-D
                @ARG
                M=D
                """;
        assembly = assembly + String.format(calleeArg, nArgs);

        String calleeLCL = """
                // set callee LCL
                @SP
                D=M
                @LCL
                M=D
                """;

        assembly = assembly + calleeLCL;
        this.write(assembly);
        this.writeGotoOnly(functionName);
        this.writeLabel("ret." + this.callCount);
        this.callCount += 1;
    }

    void writeReturn() {
        String assembly = "";
        String topComment = "// return";
        assembly = assembly + topComment;

        String resetMemorySegment = """
                // reset memory segments for caller
                @%d
                D=A
                @LCL
                A=M-D
                D=M
                @%s
                M=D
                """;
        String returnAddress = "returnAddress";
        assembly = assembly + String.format(resetMemorySegment, 5, returnAddress);

        String pushResult = """
                // push result
                @SP
                A=M-1
                D=M
                @ARG
                A=M
                M=D
                """;
        assembly = assembly + pushResult;
        String resetSP = """
                // reset SP to ARG+1
                @ARG
                D=M+1
                @SP
                M=D
                """;
        assembly = assembly + resetSP;

        assembly = assembly + String.format(resetMemorySegment, 1, "THAT");
        assembly = assembly + String.format(resetMemorySegment, 2, "THIS");
        assembly = assembly + String.format(resetMemorySegment, 3, "ARG");
        assembly = assembly + String.format(resetMemorySegment, 4, "LCL");

        String goToReturnAddress = """
                // jump to returnAddress
                @%s
                A=M
                0;JMP
                """;
        assembly = assembly + String.format(goToReturnAddress, returnAddress);
        this.write(assembly);
    }

    private void write(String assembly) {
        try {
            System.out.println("Writing..." + assembly);
            this.bw.write(assembly);
        } catch (IOException e) {
            System.out.println("Could not write to file");
            e.printStackTrace();
        }
    }
}
