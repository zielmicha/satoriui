package net.atomshare.satori;

import net.atomshare.satori.thrift.gen.*;
import org.apache.thrift.protocol.TProtocol;

public abstract class Connection implements AutoCloseable {
    protected TProtocol protocol;

    public final User.Client user;
    public final Web.Client web;

    protected Connection(TProtocol protocol) {
        this.protocol = protocol;
        this.user = new User.Client(protocol);
        this.web = new Web.Client(protocol);
    }

    public abstract void destroy();
    public abstract void close();
}
