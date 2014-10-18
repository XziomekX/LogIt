package io.github.lucaseasedup.logit.session;

public final class SessionStartEvent extends SessionEvent
{
    /* package */ SessionStartEvent(String username, Session session)
    {
        super(username, session);
    }
}
