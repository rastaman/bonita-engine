/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package com.bonitasoft.engine.search;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.HumanTaskInstance;
import org.bonitasoft.engine.bpm.model.Index;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.UserTaskInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidExpressionException;
import org.bonitasoft.engine.exception.InvalidProcessDefinitionException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotEnabledException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.ProcessDefinitionReadException;
import org.bonitasoft.engine.exception.ProcessInstanceCreationException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.process.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.model.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.bpm.model.ProcessInstanceUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchProcessInstanceTest extends CommonAPISPTest {

    private User user;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(user);
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
        user = createUser("jane", "bpm");
    }

    @Test
    public void searchOpenProcessInstances() throws Exception {
        final String username = "matti";
        final String password = "suomenlinna";
        final User user = createUser(username, password);

        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, "Actor1", user);
        final ProcessInstance instance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance5 = getProcessAPI().startProcess(processDefinition.getId());
        // prepare searchOptions
        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 10,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);
        // search and check result ASC
        assertTrue("no started process instances are found", new WaitUntil(500, 5000) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().searchOpenProcessInstances(searchOptions.done()).getCount() == 5;
            }
        }.waitUntil());
        final SearchOptionsBuilder searchOptions1 = buildSearchOptions(processDefinition.getId(), 0, 2,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);

        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstances(searchOptions1.done());
        assertNotNull(result);
        assertEquals(5, result.getCount());
        final List<ProcessInstance> processInstanceList1 = result.getResult();
        assertNotNull(processInstanceList1);
        assertEquals(2, processInstanceList1.size());
        assertEquals(instance1.getId(), processInstanceList1.get(0).getId());
        assertEquals(instance2.getId(), processInstanceList1.get(1).getId());

        final SearchOptionsBuilder searchOptions2 = buildSearchOptions(processDefinition.getId(), 2, 2,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);
        result = getProcessAPI().searchOpenProcessInstances(searchOptions2.done());
        assertNotNull(result);
        assertEquals(5, result.getCount());
        final List<ProcessInstance> processInstanceList2 = result.getResult();
        assertNotNull(processInstanceList2);
        assertEquals(1, processInstanceList2.size());
        assertEquals(instance5.getId(), processInstanceList2.get(0).getId());

        final SearchOptionsBuilder searchOptions3 = buildSearchOptions(processDefinition.getId(), 0, 3,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.DESC);
        // search and check result DESC
        result = getProcessAPI().searchOpenProcessInstances(searchOptions3.done());
        assertNotNull(result);
        assertEquals(5, result.getCount());
        final List<ProcessInstance> processInstanceList3 = result.getResult();
        assertNotNull(processInstanceList3);
        assertEquals(3, processInstanceList3.size());
        assertEquals(instance5.getId(), processInstanceList3.get(0).getId());
        assertEquals(instance4.getId(), processInstanceList3.get(1).getId());
        assertEquals(instance3.getId(), processInstanceList3.get(2).getId());

        final SearchOptionsBuilder searchOptions4 = buildSearchOptions(processDefinition.getId() + 1, 0, 3,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.DESC);
        result = getProcessAPI().searchOpenProcessInstances(searchOptions4.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        disableAndDelete(processDefinition);
        deleteUser(user);
    }

    @Test
    public void searchOpenProcessInstancesSupervisedBy() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        // create user
        final User user1 = createUser("user1", "bpm");
        // before supervisor
        SearchOptionsBuilder searchOptions = buildSearchOptions(0, 10, org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesSupervisedBy(user1.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());
        assertNotNull(result.getResult());
        assertEquals(0, result.getResult().size());
        // after supervisor
        final ProcessSupervisor supervisor1 = createSupervisor(processDefinition.getId(), user1.getId());

        // prepare search options
        searchOptions = buildSearchOptions(0, 10, org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.NAME, Order.ASC);
        // search and check result
        result = getProcessAPI().searchOpenProcessInstancesSupervisedBy(user1.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        final List<ProcessInstance> processInstanceList = result.getResult();
        assertNotNull(processInstanceList);
        assertEquals(1, processInstanceList.size());
        assertEquals(instance.getId(), processInstanceList.get(0).getId());

        // add supervisor by role and group
        final User supervisor = createUser("supervisor", "bpm");
        final Map<String, Object> map = createSupervisorByRoleAndGroup(processDefinition.getId(), supervisor.getId());
        final ProcessSupervisor supervisorByRole = (ProcessSupervisor) map.get("supervisorByRole");
        final ProcessSupervisor supervisorByGroup = (ProcessSupervisor) map.get("supervisorByGroup");
        final Role role = (Role) map.get("roleId");
        final Group group = (Group) map.get("groupId");
        final UserMembership membership = (UserMembership) map.get("membership");
        assertEquals(supervisorByRole.getRoleId(), role.getId());
        assertEquals(supervisorByGroup.getGroupId(), group.getId());
        assertEquals(membership.getUserId(), supervisor.getId());
        assertEquals(membership.getRoleId(), role.getId());
        assertEquals(membership.getGroupId(), group.getId());
        // prepare search options
        searchOptions = buildSearchOptions(0, 10, org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.NAME, Order.ASC);
        // search and check result
        result = getProcessAPI().searchOpenProcessInstancesSupervisedBy(supervisor.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        final List<ProcessInstance> processInstanceList2 = result.getResult();
        assertNotNull(processInstanceList2);
        assertEquals(1, processInstanceList2.size());
        assertEquals(instance.getId(), processInstanceList2.get(0).getId());

        // clean-up
        deleteSupervisor(supervisor1.getSupervisorId());
        deleteRoleGroupSupervisor(map, supervisor.getId());
        deleteUser(supervisor);
        deleteUser(user1);
        disableAndDelete(processDefinition);
        deleteUser(user);
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex1AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());

        waitForStep("step2", processInstance1);

        final ProcessInstanceUpdateDescriptor updateDescriptor = new ProcessInstanceUpdateDescriptor();
        updateDescriptor.updateStringIndex1("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex1());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.FIRST, "metsassa1");
        assertEquals("metsassa1", processInstance.getStringIndex1());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_1, "metsassa1");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDelete(processDefinition);
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex2AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());

        waitForStep("step2", processInstance1);

        final ProcessInstanceUpdateDescriptor updateDescriptor = new ProcessInstanceUpdateDescriptor();
        updateDescriptor.updateStringIndex2("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex2());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.SECOND, "metsassa2");
        assertEquals("metsassa2", processInstance.getStringIndex2());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_2, "metsassa2");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDelete(processDefinition);
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex3AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());

        waitForStep("step2", processInstance1);

        final ProcessInstanceUpdateDescriptor updateDescriptor = new ProcessInstanceUpdateDescriptor();
        updateDescriptor.updateStringIndex3("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex3());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.THIRD, "metsassa3");
        assertEquals("metsassa3", processInstance.getStringIndex3());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_3, "metsassa3");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDelete(processDefinition);
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex4AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());

        waitForStep("step2", processInstance1);

        final ProcessInstanceUpdateDescriptor updateDescriptor = new ProcessInstanceUpdateDescriptor();
        updateDescriptor.updateStringIndex4("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex4());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.FOURTH, "metsassa4");
        assertEquals("metsassa4", processInstance.getStringIndex4());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_4, "metsassa4");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDelete(processDefinition);
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex5AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());

        waitForStep("step2", processInstance1);

        final ProcessInstanceUpdateDescriptor updateDescriptor = new ProcessInstanceUpdateDescriptor();
        updateDescriptor.updateStringIndex5("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex5());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.FIFTH, "metsassa5");
        assertEquals("metsassa5", processInstance.getStringIndex5());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_5, "metsassa5");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDelete(processDefinition);
    }

    /*
     * Start process not with jack
     * execute a task with jack
     * check archived process instances worked on are 0
     * finish process
     * check there is one archived process instance worked on
     */
    @Test
    public void searchArchivedProcessInstanceWorkedOnWithUserPerformedTask() throws Exception {
        // create user
        final String username = "jack";
        final String password = "bpm";
        final User jack = createUser(username, password);
        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(
                "SearchOpenProcessInstancesInvolvingUser", "14.3");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        logout();
        loginWith(username, password);
        CheckNbPendingTaskOf pendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 500, 5000, false, 1, jack);
        assertTrue(pendingTaskOf.waitUntil());
        List<HumanTaskInstance> pendingTasks = pendingTaskOf.getPendingHumanTaskInstances();
        UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);
        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);
        // assign
        assignAndExecuteStep(pendingTask, jack.getId());
        // executed but not archived
        SearchResult<ArchivedProcessInstance> result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        pendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 500, 5000, false, 1, jack);
        assertTrue(pendingTaskOf.waitUntil());
        pendingTasks = pendingTaskOf.getPendingHumanTaskInstances();
        pendingTask = (UserTaskInstance) pendingTasks.get(0);
        assertEquals("step2", pendingTask.getName());

        assignAndExecuteStep(pendingTask, jack.getId());

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        final ArchivedProcessInstance processInstance2 = result.getResult().get(0);
        assertEquals(processInstance.getId(), processInstance2.getSourceObjectId());

        deleteUser(jack);
        disableAndDelete(processDefinition);
    }

    /*
     * Start process with jack
     * execute a task with john
     * check archived process instances worked on are 0
     * finish process
     * check there is one archived process instance worked on
     */
    @Test
    public void searchArchivedProcessInstanceWorkedOnWithUserStartedProcess() throws Exception {
        // create user
        final String password = "bpm";
        final User jack = createUser("jack", password);
        final User john = createUser("john", password);
        logout();
        loginWith("john", password);
        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(
                "SearchOpenProcessInstancesInvolvingUser", "15.3");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final CheckNbPendingTaskOf pendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 500, 5000, false, 1, jack);
        assertTrue(pendingTaskOf.waitUntil());
        final List<HumanTaskInstance> pendingTasks = pendingTaskOf.getPendingHumanTaskInstances();
        final UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);
        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);

        // the process is not started by jack but not finished: not in "workedOn"
        SearchResult<ArchivedProcessInstance> result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());
        // assign
        assignAndExecuteStep(pendingTask, jack.getId());

        // process finished: in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());

        deleteUser(jack);
        deleteUser(john);
        disableAndDelete(processDefinition);
    }

    /*
     * Start process not with jack
     * assign task to jack
     * check worked on = 0
     * execute task with jack
     * check worked on = 1
     * finish process
     * check worked on = 0
     */
    @Test
    public void searchProcessInstanceWorkedOnWithUserPerformedTask() throws Exception {
        // create user
        final String username = "jack";
        final String password = "bpm";
        final User jack = createUser(username, password);
        final User john = createUser("john", password);
        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(
                "SearchOpenProcessInstancesInvolvingUser", "16.3");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        logout();
        loginWith(username, password);
        CheckNbPendingTaskOf pendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 500, 5000, false, 1, jack);
        assertTrue(pendingTaskOf.waitUntil());
        List<HumanTaskInstance> pendingTasks = pendingTaskOf.getPendingHumanTaskInstances();
        UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);
        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);

        // the process is not started by jack and jack has not performed tasks: not in "workedOn"
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());
        // assign
        getProcessAPI().assignUserTask(pendingTask.getId(), jack.getId());
        // after assigned: still not worked on
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        getProcessAPI().executeActivity(pendingTask.getId());

        pendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 500, 5000, false, 1, jack);
        assertTrue(pendingTaskOf.waitUntil());
        pendingTasks = pendingTaskOf.getPendingHumanTaskInstances();
        pendingTask = (UserTaskInstance) pendingTasks.get(0);
        assertEquals("step2", pendingTask.getName());

        // one task was performed: the process is in "WorkedOn"
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        final ProcessInstance processInstance2 = result.getResult().get(0);
        assertEquals(processInstance.getId(), processInstance2.getId());
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        getProcessAPI().assignUserTask(pendingTask.getId(), jack.getId());
        getProcessAPI().executeActivity(pendingTask.getId());
        getProcessAPI().executeActivity(pendingTask.getId());

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        deleteUser(john);
        deleteUser(jack);
        disableAndDelete(processDefinition);
    }

    @Test
    public void searchProcessInstanceWorkedOnWithUserPerformedTaskOnMultipleInstances() throws Exception {
        // create user
        final String username = "jack";
        final String password = "bpm";
        final User jack = createUser(username, password);
        final User john = createUser("john", password);
        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(
                "SearchOpenProcessInstancesInvolvingUser", "16.3");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p4 = getProcessAPI().startProcess(processDefinition.getId());

        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);

        logout();
        loginWith(username, password);
        waitForUserTaskAndExecuteIt("step1", p1, jack.getId());
        waitForUserTaskAndExecuteIt("step1", p2, jack.getId());
        waitForUserTaskAndExecuteIt("step1", p3, jack.getId());
        logout();
        loginWith("john", password);
        waitForUserTaskAndExecuteIt("step1", p4, john.getId());

        waitForPendingTasks(jack.getId(), 4);

        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(3, result.getCount());
        assertEquals(3, result.getResult().size());

        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals(1, result.getResult().size());

        deleteUser(john);
        deleteUser(jack);
        disableAndDelete(processDefinition);
    }

    @Test
    public void searchProcessInstanceWorkedOnWithUserStartedItOnMultipleInstances() throws Exception {
        // create user
        final String username = "jack";
        final String password = "bpm";
        final User jack = createUser(username, password);
        final User john = createUser("john", password);
        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(
                "SearchOpenProcessInstancesInvolvingUser", "16.3");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        logout();
        loginWith(username, password);
        getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());
        logout();
        loginWith("john", password);
        getProcessAPI().startProcess(processDefinition.getId());

        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);

        waitForPendingTasks(jack.getId(), 4);

        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(3, result.getCount());
        assertEquals(3, result.getResult().size());

        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals(1, result.getResult().size());

        deleteUser(john);
        deleteUser(jack);
        disableAndDelete(processDefinition);
    }

    /*
     * Start process with jack
     * assign/execute task with john
     * check worked on = 1 for jack
     * finish process
     * check worked on = 0
     */
    @Test
    public void searchProcessInstanceWorkedOnWithUserStartedProcess() throws Exception {
        // create user
        final String password = "bpm";
        final User jack = createUser("jack", password);
        final User john = createUser("john", password);
        logout();
        loginWith("john", password);
        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = createProcessDefinition("SearchOpenProcessInstancesInvolvingUser", delivery, true, null,
                null, null, null, null);
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final CheckNbPendingTaskOf pendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 500, 5000, false, 1, jack);
        assertTrue(pendingTaskOf.waitUntil());
        final List<HumanTaskInstance> pendingTasks = pendingTaskOf.getPendingHumanTaskInstances();
        final UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);
        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);

        // the process is started by jack and jack has not performed tasks: In "workedOn"
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assignAndExecuteStep(pendingTask, jack.getId());

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        deleteUser(jack);
        deleteUser(john);
        disableAndDelete(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Managed By", "Process instance", "Open", "Involving User",
            "User Performed Task" }, jira = "ENGINE-715")
    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedByWithUserPerformedTask() throws Exception {
        // create user
        final User paul = createUser("paul", "bpm");
        final User jack = createUserWithManager("jack", paul.getId());
        final User john = createUser("john", "bpm");

        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(
                "SearchOpenProcessInstancesInvolvingUser", "16.3");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        logout();
        loginWith("jack", "bpm");
        CheckNbPendingTaskOf pendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 500, 5000, false, 1, jack);
        assertTrue(pendingTaskOf.waitUntil());
        List<HumanTaskInstance> pendingTasks = pendingTaskOf.getPendingHumanTaskInstances();
        UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);
        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);

        // the process is not started by jack and jack has not performed tasks: not in "workedOn"
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());
        // assign
        getProcessAPI().assignUserTask(pendingTask.getId(), jack.getId());
        // after assigned: still not worked on
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        getProcessAPI().executeActivity(pendingTask.getId());

        pendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 500, 5000, false, 1, jack);
        assertTrue(pendingTaskOf.waitUntil());
        pendingTasks = pendingTaskOf.getPendingHumanTaskInstances();
        pendingTask = (UserTaskInstance) pendingTasks.get(0);
        assertEquals("step2", pendingTask.getName());

        // one task was performed: the process is in "WorkedOn"
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        final ProcessInstance processInstance2 = result.getResult().get(0);
        assertEquals(processInstance.getId(), processInstance2.getId());

        assignAndExecuteStep(pendingTask, jack.getId());

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        deleteUser(john);
        deleteUser(jack);
        deleteUser(paul);
        disableAndDelete(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Managed By", "Process instance", "Open", "Involving User",
            "User Performed Task", "Multiple Instances" }, jira = "ENGINE-715")
    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedByWithUserPerformedTaskOnMultipleInstances() throws Exception {
        // create user
        final User paul = createUser("paul", "bpm");
        final User jack = createUserWithManager("jack", paul.getId());
        final User john = createUserWithManager("john", paul.getId());

        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(
                "SearchOpenProcessInstancesInvolvingUser", "16.3");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p4 = getProcessAPI().startProcess(processDefinition.getId());

        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);

        logout();
        loginWith("jack", "bpm");
        waitForUserTaskAndExecuteIt("step1", p1, jack.getId());
        waitForUserTaskAndExecuteIt("step1", p2, jack.getId());
        waitForUserTaskAndExecuteIt("step1", p3, jack.getId());
        logout();
        loginWith("john", "bpm");
        waitForUserTaskAndExecuteIt("step1", p4, john.getId());

        waitForPendingTasks(jack.getId(), 4);

        final SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(4, result.getCount());
        assertEquals(4, result.getResult().size());

        deleteUser(john);
        deleteUser(jack);
        deleteUser(paul);
        disableAndDelete(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Managed By", "Process instance", "Open", "Involving User",
            "User Started It", "Multiple Instances" }, jira = "ENGINE-715")
    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedByWithUserStartedItOnMultipleInstances() throws Exception {
        // create user
        final User paul = createUser("paul", "bpm");
        final User jack = createUserWithManager("jack", paul.getId());
        final User john = createUserWithManager("john", paul.getId());
        final User pierre = createUser("pierre", "bpm");
        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(
                "SearchOpenProcessInstancesInvolvingUser", "16.3");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        logout();
        loginWith("jack", "bpm");
        getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());
        logout();
        loginWith("john", "bpm");
        getProcessAPI().startProcess(processDefinition.getId());
        logout();
        loginWith("pierre", "bpm");
        getProcessAPI().startProcess(processDefinition.getId());

        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);
        waitForPendingTasks(jack.getId(), 5);

        final SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(4, result.getCount());
        assertEquals(4, result.getResult().size());

        deleteUser(john);
        deleteUser(jack);
        deleteUser(paul);
        deleteUser(pierre);
        disableAndDelete(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Managed By", "Process instance", "Open", "Involving User",
            "User Started Process" }, jira = "ENGINE-715")
    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedByWithUserStartedProcess() throws Exception {
        // create user
        final User paul = createUser("paul", "bpm");
        final User jack = createUserWithManager("jack", paul.getId());
        final User john = createUserWithManager("john", paul.getId());
        logout();
        loginWith("john", "bpm");
        // create process
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = createProcessDefinition("SearchOpenProcessInstancesInvolvingUser", delivery, true, null,
                null, null, null, null);
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final CheckNbPendingTaskOf pendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 500, 5000, false, 1, jack);
        assertTrue(pendingTaskOf.waitUntil());
        final List<HumanTaskInstance> pendingTasks = pendingTaskOf.getPendingHumanTaskInstances();
        final UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);
        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);

        // the process is started by jack and jack has not performed tasks: In "workedOn"
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assignAndExecuteStep(pendingTask, jack.getId());

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        deleteUser(jack);
        deleteUser(john);
        deleteUser(paul);
        disableAndDelete(processDefinition);
    }

    @Test
    public void searchOpenProcessInstancesStartedBy() throws Exception {
        final String USERNAME = "jack";
        final String PASSWORD = "bpm";
        final User user = getIdentityAPI().createUser(USERNAME, PASSWORD);
        // change login user to jack
        logout();
        loginWith(USERNAME, PASSWORD);

        // create process
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, "Actor1", user);
        final ProcessInstance instance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance5 = getProcessAPI().startProcess(processDefinition.getId());
        // prepare searchOptions
        final SearchOptionsBuilder searchOptions = buildSearchOptions(processDefinition.getId(), 0, 5,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);
        // search and check result ASC
        assertTrue("no pending user task instances are found", new WaitUntil(500, 5000) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().searchOpenProcessInstances(searchOptions.done()).getCount() == 5;
            }
        }.waitUntil());
        // test started by correct user
        final SearchOptionsBuilder searchOptions1 = buildSearchOptions(processDefinition.getId(), 0, 10,
                org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.ID, Order.ASC);
        searchOptions1.filter(org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.STARTED_BY, user.getId());
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstances(searchOptions1.done());
        assertNotNull(result);
        assertEquals(5, result.getCount());
        final List<ProcessInstance> processInstanceList1 = result.getResult();
        assertNotNull(processInstanceList1);
        assertEquals(5, processInstanceList1.size());
        assertEquals(instance1.getId(), processInstanceList1.get(0).getId());
        assertEquals(instance2.getId(), processInstanceList1.get(1).getId());
        assertEquals(instance3.getId(), processInstanceList1.get(2).getId());
        assertEquals(instance4.getId(), processInstanceList1.get(3).getId());
        assertEquals(instance5.getId(), processInstanceList1.get(4).getId());

        // test started by not existed user
        searchOptions1.filter(org.bonitasoft.engine.search.ProcessInstanceSearchDescriptor.STARTED_BY, user.getId() + 1500);
        result = getProcessAPI().searchOpenProcessInstances(searchOptions1.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        disableAndDelete(processDefinition);
        deleteUser(user.getId());
    }

    @Test
    public void searchArchivedProcessInstancesInvolvingUser() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(false));
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);

        logout();
        loginWith("john", "bpm");
        final ProcessInstance instance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance5 = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(instance1);
        waitForProcessToFinish(instance2);
        waitForProcessToFinish(instance3);
        waitForProcessToFinish(instance4);
        waitForProcessToFinish(instance5);
        // test started by correct user
        final SearchOptionsBuilder opts = new SearchOptionsBuilder(0, 10);
        opts.filter(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        opts.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, Order.ASC);
        final SearchResult<ArchivedProcessInstance> result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(user.getId(), opts.done());
        assertEquals(5, result.getCount());
        final List<ArchivedProcessInstance> processInstances = result.getResult();
        assertNotNull(processInstances);
        assertEquals(5, processInstances.size());
        assertEquals(instance1.getId(), processInstances.get(0).getSourceObjectId());
        assertEquals(instance2.getId(), processInstances.get(1).getSourceObjectId());
        assertEquals(instance3.getId(), processInstances.get(2).getSourceObjectId());
        assertEquals(instance4.getId(), processInstances.get(3).getSourceObjectId());
        assertEquals(instance5.getId(), processInstances.get(4).getSourceObjectId());

        disableAndDelete(processDefinition);
        deleteUser(user.getId());
    }

    @Cover(classes = { ProcessAPI.class, ArchivedProcessInstancesSearchDescriptor.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Archived",
            "Process Instances" }, jira = "ENGINE-998")
    @Test
    public void searchArchivedProcessInstances() throws Exception {
        final User user1 = createUser("john1", "bpm");
        final User user2 = createUser("john2", "bpm");
        final User user3 = createUser("john3", "bpm");
        final User user4 = createUser("john4", "bpm");

        final String delivery = "delivery";
        final DesignProcessDefinition designProcessDefinition1 = createProcessDefinition("3", delivery, false, "value1", "value2", "value3", "value4", "value5")
                .done();
        // final ProcessDefinition processDefinition1 = deployAndEnableWithActor(designProcessDefinition1, delivery, user1);
        final ProcessDefinition processDefinition1 = deployAndEnableProcess(designProcessDefinition1);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition1.getId());
        waitForProcessToFinish(processInstance1);
        logout();

        loginWith("john1", "bpm");
        final DesignProcessDefinition designProcessDefinition2 = createProcessDefinition("2", delivery, false, "value2", "value4", "value1", "value5", "value3")
                .done();
        final ProcessDefinition processDefinition2 = deployAndEnableProcess(designProcessDefinition2);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        waitForProcessToFinish(processInstance2);
        logout();

        loginWith("john3", "bpm");
        final DesignProcessDefinition designProcessDefinition3 = createProcessDefinition("5", delivery, false, "value4", "value3", "value5", "value2", "value1")
                .done();
        final ProcessDefinition processDefinition3 = deployAndEnableProcess(designProcessDefinition3);
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition3.getId());
        waitForProcessToFinish(processInstance3);
        logout();

        loginWith("john2", "bpm");
        final DesignProcessDefinition designProcessDefinition4 = createProcessDefinition("4", delivery, false, "value5", "value1", "value4", "value3", "value2")
                .done();
        final ProcessDefinition processDefinition4 = deployAndEnableProcess(designProcessDefinition4);
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDefinition4.getId());
        waitForProcessToFinish(processInstance4);
        logout();

        loginWith("john4", "bpm");
        final DesignProcessDefinition designProcessDefinition5 = createProcessDefinition("1", delivery, false, "value3", "value5", "value2", "value1", "value4")
                .done();
        final ProcessDefinition processDefinition5 = deployAndEnableProcess(designProcessDefinition5);
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDefinition5.getId());
        waitForProcessToFinish(processInstance5);

        // Order by ARCHIVE_DATE
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        // searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.ARCHIVE_DATE, Order.ASC);
        final SearchResult<ArchivedProcessInstance> result = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done());
        assertEquals(5, result.getCount());
        List<ArchivedProcessInstance> archivedProcessInstances = result.getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by ASSIGNEE_ID
        // searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        // searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.ASSIGNEE_ID, Order.ASC);
        // result = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done());
        // assertEquals(5, result.getCount());
        // archivedProcessInstances = result.getResult();
        // assertNotNull(archivedProcessInstances);
        // assertEquals(5, archivedProcessInstances.size());
        // assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        // assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        // assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        // assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        // assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by END_DATE
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.END_DATE, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by GROUP_ID
        // searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        // searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.GROUP_ID, Order.ASC);
        // archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        // assertNotNull(archivedProcessInstances);
        // assertEquals(5, archivedProcessInstances.size());
        // assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        // assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        // assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        // assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        // assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by ID
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.ID, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by LAST_UPDATE
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.LAST_UPDATE, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by NAME
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.NAME, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by PROCESS_DEFINITION_ID
        // searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        // searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, Order.ASC);
        // archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        // assertNotNull(archivedProcessInstances);
        // assertEquals(5, archivedProcessInstances.size());
        // assertEquals("The process must be " + processInstance1.getName() + ", but was " + archivedProcessInstances.get(0).getName(),
        // processInstance1.getId(),
        // archivedProcessInstances.get(0).getSourceObjectId());
        // assertEquals("The process must be " + processInstance5.getName() + ", but was " + archivedProcessInstances.get(1).getName(),
        // processInstance5.getId(),
        // archivedProcessInstances.get(1).getSourceObjectId());
        // assertEquals("The process must be " + processInstance2.getName() + ", but was " + archivedProcessInstances.get(2).getName(),
        // processInstance2.getId(),
        // archivedProcessInstances.get(2).getSourceObjectId());
        // assertEquals("The process must be " + processInstance4.getName() + ", but was " + archivedProcessInstances.get(3).getName(),
        // processInstance4.getId(),
        // archivedProcessInstances.get(3).getSourceObjectId());
        // assertEquals("The process must be " + processInstance3.getName() + ", but was " + archivedProcessInstances.get(4).getName(),
        // processInstance3.getId(),
        // archivedProcessInstances.get(4).getSourceObjectId());

        // Order by ROLE_ID
        // searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        // searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.ROLE_ID, Order.ASC);
        // archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        // assertNotNull(archivedProcessInstances);
        // assertEquals(5, archivedProcessInstances.size());
        // assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        // assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        // assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        // assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        // assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by SOURCE_OBJECT_ID
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by START_DATE
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.START_DATE, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STARTED_BY
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.STARTED_BY, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STATE_ID
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.STATE_ID, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STRING_INDEX_1
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_1, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STRING_INDEX_2
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_2, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STRING_INDEX_3
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_3, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STRING_INDEX_4
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_4, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STRING_INDEX_5
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_5, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Search term for STRING_INDEX
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.ID, Order.ASC);
        searchOptionsBuilder.searchTerm("value1");
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by USER_ID
        // searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        // searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.USER_ID, Order.ASC);
        // archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        // assertNotNull(archivedProcessInstances);
        // assertEquals(5, archivedProcessInstances.size());
        // assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        // assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        // assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        // assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        // assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        disableAndDeleteProcess(processDefinition1, processDefinition2, processDefinition3, processDefinition4, processDefinition5);
        deleteUsers(user1, user2, user3, user4);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "API, Search" }, story = "Search process instance should return 1 when there's a subprocess and parent process active", jira = "ENGINE-964")
    @Test
    public void twoPoolsWithOneWithACallActivityCaseTest() throws Exception {
        final User user = createUser("USERNAME", "PASSWORD");

        final ProcessDefinitionBuilderExt process2DefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("process2", "1.0");
        final String actorName = "Actor";
        process2DefinitionBuilder.addActor(actorName);
        process2DefinitionBuilder.addUserTask("User task", actorName);
        final DesignProcessDefinition designProcess2Definition = process2DefinitionBuilder.done();
        final ProcessDefinition process2Definition = deployAndEnableWithActor(designProcess2Definition, actorName, user);

        final ProcessDefinitionBuilderExt process1DefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("process1", "1.0");
        process1DefinitionBuilder.addActor(actorName);

        final Expression process2Name = new ExpressionBuilder().createConstantStringExpression("process2");
        final Expression process2Version = new ExpressionBuilder().createConstantStringExpression("1.0");
        process1DefinitionBuilder.addCallActivity("call process2", process2Name, process2Version);
        final DesignProcessDefinition designProcess1Definition = process1DefinitionBuilder.done();
        final ProcessDefinition process1Definition = deployAndEnableWithActor(designProcess1Definition, actorName, user);

        final ProcessInstance instance1 = getProcessAPI().startProcess(process1Definition.getId());
        waitForUserTask("User task", instance1.getId());

        final SearchOptions opts = new SearchOptionsBuilder(0, 10).done();

        final SearchResult<ProcessInstance> processInstanceSearchResult = getProcessAPI().searchOpenProcessInstances(opts);

        assertThat(processInstanceSearchResult.getCount(), is(1L));

        disableAndDelete(process2Definition);
        disableAndDelete(process1Definition);
        deleteUser(user);
    }

    private ProcessDefinitionBuilderExt createProcessDefinition(final String processName, final String delivery, final boolean withUserTask,
            final String stringIndex1, final String stringIndex2, final String stringIndex3, final String stringIndex4, final String stringIndex5)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(processName, "17.3");
        designProcessDefinition.addDescription("Delivery all day and night long");
        if (withUserTask) {
            designProcessDefinition.addActor(delivery);
            designProcessDefinition.addUserTask("step1", delivery);
        } else {
            designProcessDefinition.addAutomaticTask("step1");
        }

        if (stringIndex1 != null && stringIndex1 != "") {
            designProcessDefinition.setStringIndex(1, "label1", new ExpressionBuilder().createConstantStringExpression(stringIndex1));
        }
        if (stringIndex2 != null && stringIndex2 != "") {
            designProcessDefinition.setStringIndex(2, "label2", new ExpressionBuilder().createConstantStringExpression(stringIndex2));
        }
        if (stringIndex3 != null && stringIndex3 != "") {
            designProcessDefinition.setStringIndex(3, "label3", new ExpressionBuilder().createConstantStringExpression(stringIndex3));
        }
        if (stringIndex4 != null && stringIndex4 != "") {
            designProcessDefinition.setStringIndex(4, "label4", new ExpressionBuilder().createConstantStringExpression(stringIndex4));
        }
        if (stringIndex5 != null && stringIndex5 != "") {
            designProcessDefinition.setStringIndex(5, "label5", new ExpressionBuilder().createConstantStringExpression(stringIndex5));
        }
        return designProcessDefinition;
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchArchivedProcessInstances",
            "Apostrophe" }, jira = "ENGINE-366, ENGINE-589")
    @Test
    public void searchArchivedProcessInstancesWithApostrophe() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        // Create process
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("Na'me", PROCESS_VERSION);
        processBuilder.addDescription(DESCRIPTION).addDisplayDescription(DESCRIPTION).addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // Archive a ProcessInstance
        waitForStep("userTask1", processInstance);

        final Set<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activityInstances.size());
        for (final ActivityInstance activityInstance : activityInstances) {
            final long activityInstanceId = activityInstance.getId();
            getProcessAPI().setActivityStateById(activityInstanceId, 12);
        }

        // Search Archived process
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10).searchTerm("Na'");
        final SearchResult<ArchivedProcessInstance> searchProcessInstanceResult = getProcessAPI().searchArchivedProcessInstances(builder.done());
        assertEquals(1, searchProcessInstanceResult.getCount());
        final List<ArchivedProcessInstance> archivedProcessInstances = searchProcessInstanceResult.getResult();
        final ArchivedProcessInstance archivedProcessInstance = archivedProcessInstances.get(0);
        assertEquals(processInstance.getId(), archivedProcessInstance.getSourceObjectId());
        assertEquals("Na'me", archivedProcessInstance.getName());

        // Clean
        disableAndDelete(processDefinition);
        deleteUser(user);
    }

    @Test
    public void searchArchivedProcessInstancesSupervisedBy() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // add supervisor
        final ProcessSupervisor supervisor1 = createSupervisor(processDefinition.getId(), user.getId());

        waitForStep(500, 1000, "userTask1", processInstance);
        final Set<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        for (final ActivityInstance activityInstance : activityInstances) {
            final long activityInstanceId = activityInstance.getId();
            skipTask(activityInstanceId);
        }

        assertTrue("Expected process instance with id " + processInstance.getId() + " should be ARCHIVED", new WaitUntil(400, 3000) {

            @Override
            protected boolean check() throws Exception {
                final SearchOptions searchOpts = new SearchOptionsBuilder(0, 1).filter(
                        org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstance.getId()).done();
                return getProcessAPI().searchArchivedProcessInstances(searchOpts).getCount() == 1;
            }
        }.waitUntil());

        // test supervisor
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        final SearchResult<ArchivedProcessInstance> sapi = getProcessAPI().searchArchivedProcessInstancesSupervisedBy(user.getId(), builder.done());
        assertEquals(1, sapi.getCount());
        final List<ArchivedProcessInstance> archivedProcessInstanceList = sapi.getResult();
        assertEquals(processInstance.getId(), archivedProcessInstanceList.get(0).getSourceObjectId());

        // add supervisor by role and group
        final User supervisor = createUser("supervisor", "bpm");
        final Map<String, Object> map = createSupervisorByRoleAndGroup(processDefinition.getId(), supervisor.getId());
        final ProcessSupervisor supervisorByRole = (ProcessSupervisor) map.get("supervisorByRole");
        final ProcessSupervisor supervisorByGroup = (ProcessSupervisor) map.get("supervisorByGroup");
        final Role role = (Role) map.get("roleId");
        final Group group = (Group) map.get("groupId");
        final UserMembership membership = (UserMembership) map.get("membership");
        assertEquals(supervisorByRole.getRoleId(), role.getId());
        assertEquals(supervisorByGroup.getGroupId(), group.getId());
        assertEquals(membership.getUserId(), supervisor.getId());
        assertEquals(membership.getRoleId(), role.getId());
        assertEquals(membership.getGroupId(), group.getId());

        final SearchOptionsBuilder builder1 = new SearchOptionsBuilder(0, 10);
        final SearchResult<ArchivedProcessInstance> sapi1 = getProcessAPI().searchArchivedProcessInstancesSupervisedBy(supervisor.getId(), builder1.done());
        assertEquals(1, sapi1.getCount());
        final List<ArchivedProcessInstance> archivedProcessInstanceList1 = sapi.getResult();
        assertEquals(processInstance.getId(), archivedProcessInstanceList1.get(0).getSourceObjectId());

        deleteSupervisor(supervisor1.getSupervisorId());
        deleteRoleGroupSupervisor(map, supervisor.getId());
        deleteUser(supervisor);
        deleteUser(user);
        disableAndDelete(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Process instance", "terminal state" }, story = "Search archived process instances retrieve only instances in terminal state", jira = "ENGINE-1084")
    @Test
    public void testSearchArchivedProcessInstancesRetrieveOnlyTerminalStates() throws Exception {
        // create a process instance in state completed and a process instance in the state canceled
        final ProcessDefinition simpleProcess = createArchivedProcessInstanceInStateCompletedAndCanceled();

        // create process instance in state aborted
        final ProcessDefinition procWithEventSubProcess = createArchivedProcInstInAbortedState();

        // search
        final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        searchBuilder.sort(org.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor.STATE_ID, Order.ASC);

        final SearchResult<ArchivedProcessInstance> searchResult = getProcessAPI().searchArchivedProcessInstances(searchBuilder.done());
        // check the result: 3 instances are expected, one in the state completed, one in the state canceled, and one in the state aborted
        assertEquals(3, searchResult.getCount());
        final List<ArchivedProcessInstance> processes = searchResult.getResult();
        // canceled
        assertEquals(simpleProcess.getName(), processes.get(0).getName());
        assertEquals(TestStates.getCancelledState(), processes.get(0).getState());
        // aborted
        assertEquals(procWithEventSubProcess.getName(), processes.get(1).getName());
        assertEquals(TestStates.getAbortedState(), processes.get(1).getState());
        // completed
        assertEquals(simpleProcess.getName(), processes.get(2).getName());
        assertEquals(TestStates.getNormalFinalState(null), processes.get(2).getState());

        // clean up
        disableAndDelete(simpleProcess.getId());
        disableAndDelete(procWithEventSubProcess.getId());
    }

    private ProcessDefinition createArchivedProcInstInAbortedState() throws Exception {

        final String actorName = "delivery";
        final String userTaskName = "step1";
        final String subProcTaskName = "subStep";
        final String signalName = "go";
        // deploy and start a process with event subprocess
        final ProcessDefinition procWithEventSubProcess = deployProcessWithEventSubProcess(actorName, userTaskName, subProcTaskName, signalName);
        final ProcessInstance procInstWithEventSubProc = getProcessAPI().startProcess(procWithEventSubProcess.getId());

        // wait for first step of parent process and send a signal that will launch the event sub-process
        waitForUserTask(userTaskName, procInstWithEventSubProc.getId());
        getProcessAPI().sendSignal(signalName);

        // execute user task and wait the parent process to finish (state aborted)
        waitForUserTaskAndExecuteIt(subProcTaskName, procInstWithEventSubProc, user.getId());
        waitForProcessToFinish(procInstWithEventSubProc, TestStates.getAbortedState());
        return procWithEventSubProcess;
    }

    private ProcessDefinition createArchivedProcessInstanceInStateCompletedAndCanceled() throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDefinitionReadException, ProcessDefinitionNotEnabledException, ProcessInstanceCreationException, Exception {
        final String actorName = "delivery";
        final String userTaskName = "step1";
        // deploy and start simple process
        final ProcessDefinition simpleProcess = deployProcessWithHumanTask(actorName, userTaskName);
        final ProcessInstance processInstanceToComplete = getProcessAPI().startProcess(simpleProcess.getId());

        // execute user task and wait process to finish: the process will be in the state completed
        waitForUserTaskAndExecuteIt(userTaskName, processInstanceToComplete, user.getId());
        waitForProcessToFinish(processInstanceToComplete);

        // start another instance and cancel it: the process will be in the state canceled
        final ProcessInstance processInstanceToCancel = getProcessAPI().startProcess(simpleProcess.getId());
        waitForUserTask(userTaskName, processInstanceToCancel.getId());
        getProcessAPI().cancelProcessInstance(processInstanceToCancel.getId());
        waitForProcessToFinish(processInstanceToCancel, TestStates.getCancelledState());
        return simpleProcess;
    }

    private ProcessDefinition deployProcessWithHumanTask(final String actorName, final String userTaskName) throws BonitaException,
            InvalidProcessDefinitionException {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("myProc", "1.0");
        builder.addActor(actorName);
        builder.addUserTask(userTaskName, actorName);
        return deployAndEnableWithActor(builder.done(), actorName, user);
    }

    private ProcessDefinition deployProcessWithEventSubProcess(final String actorName, final String parentUserTaskName, final String suProcTaskName,
            final String signalName) throws BonitaException, InvalidProcessDefinitionException {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("eventProc", "1.0");
        builder.addActor(actorName);
        builder.addUserTask(parentUserTaskName, actorName);
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("sub", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("start").addSignalEventTrigger(signalName);
        subProcessBuilder.addUserTask(suProcTaskName, actorName);
        subProcessBuilder.addTransition("start", suProcTaskName);
        return deployAndEnableWithActor(builder.done(), actorName, user);
    }
}
