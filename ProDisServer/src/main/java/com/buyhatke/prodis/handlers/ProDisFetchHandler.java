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
 * ProDisFetchHandler subscribes to and handles the CacheFetch Event.
 */
@Component
public class ProDisFetchHandler implements Function<Event<String>, Entry> {
    private final Logger logger = LoggerFactory.getLogger(ProDisFetchHandler.class);
    private ProDictFacade proDictFacade;

    @Autowired
    public ProDisFetchHandler(Reactor reactor, ProDictFacade proDictFacade) {
        this.proDictFacade = proDictFacade;
        reactor.receive($(EventType.CACHE_FETCH), this);
        logger.info("Event Registered, " + EventType.CACHE_FETCH);
    }

    @Override
    public Entry apply(Event<String> event) {
        if(event != null) {
            String key = event.getData();
            logger.debug("Fetch Event for " + key);

            return proDictFacade.get(key);
        }
        return null;
    }
}
