package net.ninjacat.lambda.repl.cli

import com.beust.jcommander.JCommander

fun main(args: Array<String>) {
    val connectionParameters = Parameters()
    val jc = JCommander.newBuilder()
        .programName("java -jar jcqlsh.jar")
        .addObject(connectionParameters)
        .build()
    jc.parse(*args)

//    if (connectionParameters.isShowHelp()) {
//        jc.usage();
//        return;
//    }

    val repl = Repl(connectionParameters)
    repl.repl()
}