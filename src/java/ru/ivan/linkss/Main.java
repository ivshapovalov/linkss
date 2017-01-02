package ru.ivan.linkss;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import ru.ivan.linkss.repository.RedisLinkRepositoryImpl;

public class Main {

    public static void main(String[] args) {
        init();
    }

    private static void init() {

        new RedisLinkRepositoryImpl().init();

        new Thread(new Runnable() {
            @Override
            public void run() {
                new KeyCreator().create(3);
            }
        },"t3").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new KeyCreator().create(2);
            }
        },"t2").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new KeyCreator().create(6);
            }
        },"t6").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new KeyCreator().create(5);
            }
        },"t5").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new KeyCreator().create(4);
            }
        },"t4").start();

    }


}
