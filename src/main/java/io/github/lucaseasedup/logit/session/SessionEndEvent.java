package io.github.lucaseasedup.logit.session;

public final class SessionEndEvent extends SessionEvent
{
    /* package */ SessionEndEvent(String username, Session session)
    {
        super(username, session);
    }
}
