package vmtranslator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.junit.jupiter.api.Test;

public class ParserTest {
    @Test
    void testParserForArithmeticCommand() {
        // create a file
        File pathname = new File("./arth_cmd.vm");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathname));
            writer.write("add");
            writer.newLine();
            writer.write("sub");
            writer.close();
        } catch (FileAlreadyExistsException fae) {
            System.out.println("Filealready exists");
        }
        catch (IOException ioe) {
            System.out.println("Could not write to file");
        }

        try {
            Parser myParser = new Parser(pathname);
            assertTrue(myParser.hasMoreCommands());
            myParser.advance();
            assertEquals(myParser.commandType(), CommandType.C_ARITHMETIC);
            assertEquals(myParser.arg1(), "add");
            myParser.advance();
            assertEquals(myParser.arg1(), "sub");
            assertFalse(myParser.hasMoreCommands());
            // myParser.close();
            
        } catch (FileNotFoundException fne) {
            fne.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidCommandException ice) {
            ice.printStackTrace();
        } finally {
            pathname.delete();
        }


    }

    @Test
    void testParserForMemoryAccessCommand() throws Exception {
        // create a file
        File pathname = new File("./mem_cmd.vn");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathname));
            writer.write("push argument 1");
            writer.newLine();
            writer.write("pop local 2");
            writer.close();
        } catch (FileAlreadyExistsException fae) {
            System.out.println("Filealready exists");
        }
        catch (IOException ioe) {
            System.out.println("Could not write to file");
        }

        try {
            Parser myParser = new Parser(pathname);
            assertTrue(myParser.hasMoreCommands());
            myParser.advance();
            assertEquals(myParser.commandType(), CommandType.C_PUSH);
            assertEquals(myParser.arg1(), "argument");
            assertEquals(myParser.arg2(), 1);
            myParser.advance();
            assertEquals(myParser.commandType(), CommandType.C_POP);
            assertEquals(myParser.arg1(), "local");
            assertEquals(myParser.arg2(), 2);
            assertFalse(myParser.hasMoreCommands());
            myParser.close();

        } catch (FileNotFoundException fne) {
            fne.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidCommandException ice) {
            ice.printStackTrace();
        } finally {
            pathname.delete();
        }
    }

    @Test
    void testEmptyLinesAreIgnored() throws Exception {
        File pathname = new File("./withEmptyLines_cmd.vm");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathname));
            writer.write("    ");
            writer.newLine();
            writer.newLine();
            writer.write("push argument 2");
            writer.close();
        } catch (FileAlreadyExistsException fae) {
            System.out.println("Filealready exists");
        }
        catch (IOException ioe) {
            System.out.println("Could not write to file");
        }

        Parser myParser = new Parser(pathname);
        assertTrue(myParser.hasMoreCommands());
        myParser.advance();
        assertEquals(myParser.commandType(), CommandType.C_PUSH);
        assertEquals(myParser.arg1(), "argument");
        assertEquals(myParser.arg2(), 2);

        pathname.delete();
    }

    @Test
    void testLinesWithCommentsAreIgnored() throws Exception {
        File pathname = new File("./withComments_cmd.vm");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathname));
            writer.write("//This is a comment");
            writer.newLine();
            writer.write("push argument 2//comment");
            writer.close();
        } catch (FileAlreadyExistsException fae) {
            System.out.println("Filealready exists");
        }
        catch (IOException ioe) {
            System.out.println("Could not write to file");
        }

        Parser myParser = new Parser(pathname);
        assertTrue(myParser.hasMoreCommands());
        myParser.advance();
        assertEquals(myParser.commandType(), CommandType.C_PUSH);
        assertEquals(myParser.arg1(), "argument");
        assertEquals(myParser.arg2(), 2);

        pathname.delete();
    }
}
