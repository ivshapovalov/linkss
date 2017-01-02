package ru.ivan.linkss;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;

/**
 * Created by Ivan on 31.12.2016.
 */
public class KeyCreator {

    private static char[] alphabet = "abcdefghijklmnopqrstuvwxyz01234956789ABCDEFGHIJKLMNOPQRSTUVWZYZ"
            .toCharArray();

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;

    public void create(final int length) {

        redisClient = RedisClient.create("redis://localhost:6379/0");
        connection = redisClient.connect();
        syncCommands = connection.sync();
        syncCommands.select(1);

        long startTime = System.nanoTime();

        for (int i = 0; i < alphabet.length; i++) {
            char[] key = new char[length];
            key[0] = alphabet[i];
            recursive(key, 1, length);
        }
        long endTime = System.nanoTime();

        connection.close();
        redisClient.shutdown();

        System.out.println(Thread.currentThread().getName()+":формирование ключей: " + ((endTime -
                startTime) / 1000000000));

    }

    private void recursive(char[] key, int index, int length) {
        for (int j = 0; j < alphabet.length; j++) {
            key[index] = alphabet[j];
            if (index == length-1) {
                addKey(String.valueOf(key));
            } else {
                int nextIndex=index+1;
                recursive(key, nextIndex, length);
            }
        }
    }

    private void addKey(String key) {
        syncCommands.set(key, "");
    }
}