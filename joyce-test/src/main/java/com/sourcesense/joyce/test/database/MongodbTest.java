package com.sourcesense.joyce.test.database;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public interface MongodbTest {

		default MongodExecutable initMongodb() throws Exception {
        String ip = "localhost";
        int port = 27020;

        ImmutableMongodConfig mongodConfig = MongodConfig
                .builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();

        MongodStarter starter = MongodStarter.getDefaultInstance();
			  MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
				return mongodExecutable;
    }

    default void stopMongodb(MongodExecutable mongodExecutable) {
        mongodExecutable.stop();
    }
}
