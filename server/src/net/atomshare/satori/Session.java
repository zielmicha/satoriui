package net.atomshare.satori;

import net.atomshare.satori.thrift.gen.*;
import org.apache.thrift.TException;

import java.util.*;

public class Session {
    private final String cred;
    private final SessionFactory factory;
    private String token;

    public Session(SessionFactory factory, String cred) {
        this.cred = cred;
        this.factory = factory;
    }

    public synchronized void init() throws TException {
        if(token == null)
            authenticate();
    }

    public List<ContestInfo> getContests() throws TException {
        return withConnection((conn) -> conn.web.Web_get_contest_list(token));
    }

    public List<ProblemMappingInfo> getProblems(long contest) throws TException {
        return withConnection((conn) ->
                conn.web.Web_get_problem_mapping_list(token, contest));
    }

    public UserStruct getCurrentUser() throws TException {
        return getPageInfo().getUser();
    }

    public PageInfo getPageInfo(long contest) throws TException {
        return withConnection((conn) ->
                conn.web.Web_get_page_info(token, contest));
    }

    public PageInfo getPageInfo() throws TException {
        return getPageInfo(0);
    }

    private void authenticate() throws TException {
        String[] credArr = cred.split(":");
        token = withConnection((conn) ->
            conn.user.User_authenticate("", credArr[0], credArr[1]));
    }

    public <T> T withConnection(SessionFactory.Producer<T> producer) throws TException {
        return factory.withConnection(producer);
    }
}
