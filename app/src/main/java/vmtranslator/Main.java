/*
 * Runs the main
 */
package vmtranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InvalidCommandException {
        String filename = "./app/src/main/resources/BasicTest.vm";
        File pathname = new File(filename);
        String[] spl = filename.split(".vm");
        String outputFilename =  spl[0] + ".asm";
        try {
            Parser parser = new Parser(pathname);
            CodeWriter cw = new CodeWriter(outputFilename);
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
                    default:
                        System.out.println("Command not supported. Skipping:" + parser.commandType().toString());
                }
            }
            parser.close();
            cw.close();
        } catch (FileNotFoundException fne) {
            System.out.print("File not exists");
            fne.printStackTrace();
        }
    }
}
