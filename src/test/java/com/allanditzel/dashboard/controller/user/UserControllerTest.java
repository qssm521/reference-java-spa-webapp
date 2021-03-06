package com.allanditzel.dashboard.controller.user;

import com.allanditzel.dashboard.exception.UnknownResourceException;
import com.allanditzel.dashboard.model.User;
import com.allanditzel.dashboard.model.resource.UserResource;
import com.allanditzel.dashboard.model.resource.UserResourceAssembler;
import com.allanditzel.dashboard.exception.ApplicationException;
import com.allanditzel.dashboard.service.user.UserService;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountCriteria;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.group.Group;
import com.stormpath.sdk.group.GroupCriteria;
import com.stormpath.sdk.group.GroupList;
import com.sun.org.apache.bcel.internal.classfile.Unknown;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SecurityContextHolder.class, RandomStringUtils.class})
public class UserControllerTest {
    private static final String USERNAME = "username";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";

    @Mock
    private UserService userService;

    @Mock
    private UserResourceAssembler resourceAssembler;

    @Mock
    private Authentication authentication;

    @Mock
    private Application application;

    @Mock
    private AccountList accountList;

    @Mock
    private Iterator<Account> accountIterator;

    @Mock
    private Account account;

    @Mock
    private User user;

    @Mock
    private UserResource userResource;

    private UserController userController;

    @Before
    public void setUp() {
        userController = new UserController();
        ReflectionTestUtils.setField(userController, "resourceAssembler", resourceAssembler);
        ReflectionTestUtils.setField(userController, "userService", userService);
    }

    @Test(expected = UnknownResourceException.class)
    public void shouldThrowUnknownResourceExceptionIfCurrentUserCannotBeFound() throws IOException {
        String username = "username";
        String password = "password";
        UsernamePasswordAuthenticationToken currentUser = new UsernamePasswordAuthenticationToken(username, password);
        when(userService.getByUsername(username)).thenReturn(null);

        userController.redirectToCurrentUser(currentUser);
    }

    @Test
    public void shouldReturnRedirectView() throws Exception {
        String username = "username";
        String password = "password";
        String id = "id";
        String expectedRedirectView = "redirect:/api/user/" + id;
        UsernamePasswordAuthenticationToken currentUser = new UsernamePasswordAuthenticationToken(username, password);
        when(userService.getByUsername(username)).thenReturn(user);
        when(user.getId()).thenReturn(id);

        String redirectView = userController.redirectToCurrentUser(currentUser);
        assertEquals(expectedRedirectView, redirectView);

        verify(userService).getByUsername(username);
        verify(user).getId();
    }

    @Test
    public void shouldReturnUserResourceWhenUsingId() {
        String id = "id";

        when(userService.getById(id)).thenReturn(user);
        when(resourceAssembler.toResource(user)).thenReturn(userResource);

        UserResource resource = userController.getUser(id);
        assertNotNull(resource);

        verify(userService).getById(id);
        verify(resourceAssembler).toResource(user);
    }

    @Test
    public void shouldCreateUser() {
        UserResource resource = getUserResource();
        when(userService.createUser(any(User.class))).thenReturn(user);
        when(resourceAssembler.toResource(user)).thenReturn(resource);

        userController.createUser(resource);

        verify(userService).createUser(any(User.class));
        verify(resourceAssembler).toResource(user);
    }

    @Test
    public void shouldReturnEmptyListIfUserServiceReturnsEmptyList() {
        List<User> users = Collections.emptyList();
        List<UserResource> userResources = Collections.emptyList();

        when(userService.getAllUsers()).thenReturn(users);
        when(resourceAssembler.toResources(users)).thenReturn(userResources);

        List<UserResource> actualUserResources = userController.getAllUsers();
        assertNotNull(actualUserResources);
        assertEquals(0, actualUserResources.size());

        verify(userService).getAllUsers();
        verify(resourceAssembler).toResources(users);
    }

    @Test
    public void shouldReturnPopulatedListOfUserResources() {
        List<User> users = new ArrayList<>();
        users.add(getUser());
        List<UserResource> userResources = new ArrayList<>();
        userResources.add(getUserResource());

        when(userService.getAllUsers()).thenReturn(users);
        when(resourceAssembler.toResources(users)).thenReturn(userResources);

        List<UserResource> actualUserResources = userController.getAllUsers();
        assertNotNull(actualUserResources);
        assertEquals(1, actualUserResources.size());

        verify(userService).getAllUsers();
        verify(resourceAssembler).toResources(users);
    }

    private UserResource getUserResource() {
        UserResource resource = new UserResource();
        resource.setFirstName(FIRST_NAME);
        resource.setLastName(LAST_NAME);
        resource.setEmail(EMAIL);
        resource.setPassword(PASSWORD);

        return resource;
    }

    private User getUser() {
        User user = new User();
        user.setUsername(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setEmail(EMAIL);

        return user;
    }
}