package com.chesapeaketechnology.idl;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

/**
 * Root command handler, wrapper for {@link GenerateSrc} and {@link CompileSrc}.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Command(subcommands = {
        GenerateSrc.class,
        CompileSrc.class
})
public class Tool implements Callable<Void>
{
    /**
     * Launch and parse arguments.
     *
     * @param args Arguments to parse.
     */
    public static void main(String[] args)
    {
        int code = new CommandLine(new Tool()).execute(args);
        System.exit(code);
    }

    @Override
    public Void call() throws Exception
    {
        // The root command does nothing but defer to sub-commands
        return null;
    }
}
