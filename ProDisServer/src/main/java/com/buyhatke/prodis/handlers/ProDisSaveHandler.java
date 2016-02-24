package com.buyhatke.prodis.handlers;

import com.buyhatke.core.Entry;
import com.buyhatke.prodis.core.ProDictFacade;
import com.buyhatke.prodis.handlers.event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.function.Function;

import static reactor.event.selector.Selectors.$;

/**
 * ProDisSaveHandler subscribes to and handles the CacheSave Event.
 */
@Component
public class ProDisSaveHandler implements Function<Event<Entry>, Entry> {
    private final Logger logger = LoggerFactory.getLogger(ProDisSaveHandler.class);
    private ProDictFacade proDictFacade;

    @Autowired
    public ProDisSaveHandler(Reactor reactor, ProDictFacade proDictFacade) {
        this.proDictFacade = proDictFacade;
        reactor.receive($(EventType.CACHE_SAVE), this);
        logger.info("Event Registered, " + EventType.CACHE_SAVE);
    }

    @Override
    public Entry apply(Event<Entry> event) {
        if(event != null) {
            logger.debug("Save event for " + event);
            proDictFacade.put(event.getData());

            return event.getData();
        }
        return null;
    }
}
