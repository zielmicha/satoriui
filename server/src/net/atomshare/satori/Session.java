package net.atomshare.satori;

import net.atomshare.satori.thrift.gen.*;
import org.apache.thrift.TException;

import java.util.*;
import java.util.stream.*;

import javax.xml.transform.Result;

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

    public List<SubpageInfo> getNews(long contest) throws TException {
        return withConnection((conn) ->
                conn.web.Web_get_subpage_list_for_contest(token, contest, true));
    }

    public Object getResults(long contest) throws TException {
        return withConnection((conn) -> {
			long contestant = conn.web.Web_get_page_info(token, contest).contestant.id;
			List<ProblemMappingInfo> problems =
					conn.web.Web_get_problem_mapping_list(token, contest);

			Stream<ResultInfo> ret = problems.parallelStream().flatMap((problem) -> withConnection((iconn) ->
				iconn.web.Web_get_results(token, contest, contestant, problem.problem_mapping.id, 1000, 0, true)
					.results.stream()
			));

            List<ResultInfo> results = ret.map((result) -> {
				// remove unneeded info
				result.contestant = null;
				result.problem_mapping.description = null;
				return result;
			}).collect(Collectors.toList());

			results.sort((a, b) -> Comparator.<Long>naturalOrder().compare(b.submit.time, a.submit.time));

			return results;
		});
    }

    private void authenticate() throws TException {
        String[] credArr = cred.split(":");
        token = withConnection((conn) ->
            conn.user.User_authenticate("", credArr[0], credArr[1]));
    }

    public <T> T withConnection(SessionFactory.Producer<T> producer) {
        return factory.withConnection(producer);
    }

}
