/*
 * Runs the main
 */
package vmtranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InvalidCommandException {
        String filename = "SimpleFunction2";
        String ext = ".vm";
        String filedir = "./app/src/main/resources/";
        String file = filedir+ filename + ext;
        File pathname = new File(file);
        String outputFilename =  filedir + filename + ".asm";
        CodeWriter cw = new CodeWriter(outputFilename);

        // TODO
        // walk given directory for .vm files
        // for each vm file
            // create a new parser
            // set vm filename for codewriter
            // parse and write assembly code
        // TODO How to know which file to parse first? Does it matter?
        Parser parser = new Parser(pathname);
        cw.setVmFilename(filename);
        try {
            while (parser.hasMoreCommands()) {
                parser.advance();
                switch (parser.commandType()) {
                    case CommandType.C_ARITHMETIC:
                        cw.writeArithmetic(parser.arg1());
                        break;
                    case CommandType.C_PUSH:
                        cw.writePushPop("push", parser.arg1(), parser.arg2());
                        break;
                    case CommandType.C_POP:
                        cw.writePushPop("pop", parser.arg1(), parser.arg2());
                        break;
                    case CommandType.C_LABEL:
                        cw.writeLabel(parser.arg1());
                        break;
                    case CommandType.C_GOTO:
                        cw.writeGoto(parser.arg1());
                        break;
                    case CommandType.C_IF:
                        cw.writeIf(parser.arg1());
                        break;
                    case CommandType.C_FUNCTION:
                        cw.writeFunction(parser.arg1(), parser.arg2());
                        break;
                    case CommandType.C_CALL:
                        cw.writeCall(parser.arg1(), parser.arg2());
                        break;
                    case CommandType.C_RETURN:
                        cw.writeReturn();
                        break;
                    default:
                        System.out.println("Command not supported. Skipping:" + parser.commandType().toString());
                }
            }
            parser.close();
            cw.close();
        } catch (FileNotFoundException fne) {
            System.out.print("File not exists");
            fne.printStackTrace();
        } finally {
            parser.close();
            cw.close();
        }
    }
}
