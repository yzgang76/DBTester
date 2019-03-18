package com.hpe.ossm.alarmGenerator;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to manage actor system
 */

public final class ActorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActorHandler.class);
	private static ActorHandler instance;
	private static ActorSystem system;
	private static Config config;
	private static final String SELF_MONITOR_CONFIG_FILE = "alarmGenerator";
    private static final String SELF_MONITOR_ACTOR_SYSTEM_NAME = "alarmGenerator";

    /**
     * create and return instance
     * @return instance
     */
	public static synchronized ActorHandler getInstance(){
        if(instance == null) {
            instance = new ActorHandler();
        }
        return instance;
    }
	
	private ActorHandler() {
        startActorSystem();
    }
	
	private static void startActorSystem() {
		LOGGER.info("Start " + SELF_MONITOR_ACTOR_SYSTEM_NAME +" ActorSystem...");
        config = ConfigFactory.load(SELF_MONITOR_CONFIG_FILE);
        system = ActorSystem.create(SELF_MONITOR_ACTOR_SYSTEM_NAME, config);

        //LOGGER.info("Creating root actor " + SELF_MONITOR_NAME);
        //system.actorOf(ScriptActor.props(), SELF_MONITOR_NAME);
    }

    /**
     * return actor system
     * @return actor system
     */
	public ActorSystem getActorSystem() {
		return system;
	}

    /**
     *  return actor configurations
     * @return actor system configuration
     */
	
	public Config getConfig() {
		return config;
	}

    /**
     *
     * shutdown the actor system
     */
    public static void shutdownSystem() {
		LOGGER.info("Shutdown " + SELF_MONITOR_ACTOR_SYSTEM_NAME + " ActorSystem...");
        system.terminate();
    }
}
