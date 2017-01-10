package ru.ivan.linkss;

import com.lambdaworks.redis.ScoredValue;
import ru.ivan.linkss.repository.RedisLinkRepositoryImpl;

import java.util.List;

/**
 * Created by Ivan on 10.01.2017.
 */
public class TescScan {

    public static void main(String[] args) {
        List<List<String>> list =new RedisLinkRepositoryImpl().getShortStat();
        System.out.println(list);
    }
}
