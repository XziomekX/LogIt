package io.github.lucaseasedup.logit.test;

import org.bukkit.Bukkit;

public final class SelfTestB extends SelfTest
{
    @Override
    public void run() throws Exception
    {
        consoleCmd("logit");
        consoleCmd("logit ");
        consoleCmd("logit help ");
        consoleCmd("logit reload");
        consoleCmd("logit account info a");
        consoleCmd("logit account status a");
        consoleCmd("logit account rename a b");
        consoleCmd("logit backup force");
        consoleCmd("logit backup remove 1");
        consoleCmd("logit backup restore 12.db");
        consoleCmd("logit backup restore 2min");
        consoleCmd("logit gotowr");
        consoleCmd("logit ipcount 127.0.0.1");
        consoleCmd("logit ipcount asd");
        consoleCmd("logit config list a");
        consoleCmd("logit config list 2");
        consoleCmd("logit config list ");
        consoleCmd("logit version s");
        consoleCmd("logit globalpass");
        consoleCmd("logit account datum a b");
        consoleCmd("logit config set t t");
        consoleCmd("logit config get l");
        consoleCmd("logit config get locale");
        consoleCmd("logit config set locale en");
        consoleCmd("logit stats");
    }
    
    private void consoleCmd(String cmd)
    {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
}
