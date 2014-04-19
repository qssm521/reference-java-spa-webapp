package com.ditzel.dashboard.server.filter.security;

import com.ditzel.dashboard.server.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by Allan on 4/6/2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class CsrfTokenRequestBindingFilterTest {
    private CsrfTokenRequestBindingFilter csrfTokenRequestBindingFilter;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @Mock
    HttpSession session;

    @Mock
    CsrfToken token;

    @Before
    public void setUp() {
        csrfTokenRequestBindingFilter = new CsrfTokenRequestBindingFilter();
    }

    @Test
    public void handleBothSessionAndRequestTokensBeingNull() throws ServletException, IOException {
        when(request.getSession()).thenReturn(session);
        when(request.getAttribute(Constants.CSRF_TOKEN_KEY)).thenReturn(null);
        when(session.getAttribute(Constants.CSRF_TOKEN_KEY)).thenReturn(null);

        csrfTokenRequestBindingFilter.doFilterInternal(request, response, filterChain);

        verify(request).getSession();
        verify(request).getAttribute(Constants.CSRF_TOKEN_KEY);
        verify(session).getAttribute(Constants.CSRF_TOKEN_KEY);
        verifyNoMoreInteractions(request, session);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void handleRequestAttributeFoundSessionAttributeNull() throws ServletException, IOException {
        String tokenString = "token";
        String headerString = "header";

        when(request.getSession()).thenReturn(session);
        when(request.getAttribute(Constants.CSRF_TOKEN_KEY)).thenReturn(token);
        when(session.getAttribute(Constants.CSRF_TOKEN_KEY)).thenReturn(null);
        when(token.getToken()).thenReturn(tokenString);
        when(token.getHeaderName()).thenReturn(headerString);

        csrfTokenRequestBindingFilter.doFilterInternal(request, response, filterChain);

        verify(request).getSession();
        verify(request).getAttribute(Constants.CSRF_TOKEN_KEY);
        verify(session).getAttribute(Constants.CSRF_TOKEN_KEY);
        verify(session).setAttribute(Constants.CSRF_TOKEN_KEY, token);
        verify(response).setHeader(headerString, tokenString);
        verify(token).getHeaderName();
        verify(token).getToken();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void handleRequestAttributeNullSessionAttributeFound() throws ServletException, IOException {
        String tokenString = "token";
        String headerString = "header";

        when(request.getSession()).thenReturn(session);
        when(request.getAttribute(Constants.CSRF_TOKEN_KEY)).thenReturn(null);
        when(session.getAttribute(Constants.CSRF_TOKEN_KEY)).thenReturn(token);
        when(token.getToken()).thenReturn(tokenString);
        when(token.getHeaderName()).thenReturn(headerString);

        csrfTokenRequestBindingFilter.doFilterInternal(request, response, filterChain);

        verify(request).getSession();
        verify(request).getAttribute(Constants.CSRF_TOKEN_KEY);
        verify(session).getAttribute(Constants.CSRF_TOKEN_KEY);
        verify(response).setHeader(headerString, tokenString);
        verify(token).getHeaderName();
        verify(token).getToken();
        verify(filterChain).doFilter(request, response);
    }
}