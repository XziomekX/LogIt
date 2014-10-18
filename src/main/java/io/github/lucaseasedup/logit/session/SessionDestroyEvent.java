package io.github.lucaseasedup.logit.session;

public final class SessionDestroyEvent extends SessionEvent
{
    /* package */ SessionDestroyEvent(String username, Session session)
    {
        super(username, session);
    }
}
