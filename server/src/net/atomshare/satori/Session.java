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

    public List<ContestInfo> getContests() {
        return withConnection((conn) -> conn.web.Web_get_contest_list(token));
    }

    public List<ProblemMappingInfo> getProblems(long contest) throws TException {
        return withConnection((conn) -> {
            List<ProblemMappingInfo> problems = conn.web.Web_get_problem_mapping_list(token, contest);
            problems.sort((a, b) -> Comparator.<String>naturalOrder().compare(a.problem_mapping.code, b.problem_mapping.code));
            return problems;
        });
    }

    public PageInfo getPageInfo(long contest)  {
        return withConnection((conn) -> {
            PageInfo info = conn.web.Web_get_page_info(token, contest);
            for (SubpageStruct subpage : info.subpages) {
                subpage.content = RSTRenderer.render(subpage.content);
            }
            return info;
        });
    }

    public PageInfo getPageInfo() throws TException {
        return getPageInfo(0);
    }

    public List<SubpageInfo> getNews(long contest) {
        return withConnection((conn) -> {
            List<SubpageInfo> infos = conn.web.Web_get_subpage_list_for_contest(token, contest, true);
            for (SubpageInfo info : infos) {
                info.subpage.content = RSTRenderer.render(info.subpage.content);
            }
            infos.sort((a, b) -> Comparator.<Long>naturalOrder().compare(b.subpage.date_created, a.subpage.date_created));
            return infos;
        });
    }


    public Object getGlobalNews()  {
        List<ContestInfo> contests = getContests();
        Stream<SubpageInfo> ret = contests.parallelStream()
                .filter((contest) -> !contest.contest.archived && (contest.contestant != null && contest.contestant.accepted))
                .flatMap((contest) -> getNews(contest.contest.id).stream());
        List<SubpageInfo> news = ret.collect(Collectors.toList());
        news.sort((a, b) -> Comparator.<Long>naturalOrder().compare(b.subpage.date_created, a.subpage.date_created));
        return news;
    }

    public Object getResults(long contest) throws TException {
        long contestant = withConnection((conn) -> conn.web.Web_get_page_info(token, contest).contestant.id);
        List<ProblemMappingInfo> problems =
                withConnection((conn) -> conn.web.Web_get_problem_mapping_list(token, contest));

        Stream<ResultInfo> ret = problems.parallelStream().flatMap((problem) -> withConnection((conn) ->
                        conn.web.Web_get_results(token, contest, contestant, problem.problem_mapping.id, 1000, 0, true)
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
    }

    public List<ProblemMappingStruct> getAllProblems(long contest) throws TException {
        return withConnection((conn) -> {
            ProblemMappingStruct struct = new ProblemMappingStruct();
            struct.contest = contest;
            List<ProblemMappingStruct> submits = conn.problemMapping.ProblemMapping_filter(token, struct);
            return submits;
        });
    }

    private void authenticate() throws TException {
        String[] credArr = cred.split(":");
        token = withConnection((conn) ->
            conn.user.User_authenticate("", credArr[0], credArr[1]));
    }

    public <T> T withConnection(SessionFactory.Producer<T> producer) {
        return factory.withConnection((conn) -> {
            try {
                return producer.produce(conn);
            } catch(TokenInvalid ex) {
                authenticate();
                return producer.produce(conn);
            }
        });
    }
}
