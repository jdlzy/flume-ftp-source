package org.keedio.flume.source.ftp.source.sshd.server;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import org.apache.sshd.common.*;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.*;

import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.sftp.SftpSubsystem;

import java.io.IOException;
import java.nio.file.Path;
import org.keedio.flume.source.ftp.source.TestFileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.sshd.server.shell.ProcessShellFactory;

/**
 *
 * @author Luis Lázaro lalazaro@keedio.com Keedio
 */
public class EmbeddedSSHDServer {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedSSHDServer.class);
    public static Path homeDirectory;
    public static SshServer sshServer = SshServer.setUpDefaultServer();

    static {
        try {
            homeDirectory = TestFileUtils.createTmpDir();
        } catch (IOException e) {
            log.error("homeDirectoy", e);
        }
        
        sshServer.setPort(2223);
        sshServer.setHost("127.0.0.1");
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("src/test/resources/hostkey.ser"));
        sshServer.setShellFactory(new ProcessShellFactory(new String[]{"/bin/sh", "-i", "-l"}));
        sshServer.setCommandFactory(new ScpCommandFactory());
        sshServer.setFileSystemFactory(new VirtualFileSystemFactory(homeDirectory.toFile().getAbsolutePath()));
        sshServer.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(new SftpSubsystem.Factory()));
        
        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
        
        UserAuthPassword.Factory userFactory = new UserAuthPassword.Factory();
        userAuthFactories.add(userFactory);
        sshServer.setUserAuthFactories(userAuthFactories);

        sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
            public boolean authenticate(String username, String password, ServerSession session) {
                return "flumetest".equals(username) && "flumetest".equals(password);
            }
        });

    }

    @BeforeSuite
    public void initServer() throws IOException {
        sshServer.start();
    }

    @AfterSuite
    public void destroyServer() throws IOException, InterruptedException {
        if (sshServer != null && !this.sshServer.isClosed()) {
            sshServer.stop();
        }
    }
}
