package com.adobe.acs.samples.taskmanagement.impl;

import com.adobe.acs.samples.SampleExecutor;
import com.adobe.granite.taskmanagement.*;
import com.day.cq.tagging.TagManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Task Management in AEM is the set of APIs that manage Tasks, which can show up in AEM user's Inboxes.
 * Note that Workflow workitems also show up naturally in the Inbox, so if a unit of work is part of AEM Workflow, evaluate if a Task is the correct tool to manage said work.
 *
 * Javadocs:
 * - Task Management Package: https://docs.adobe.com/docs/en/aem/6-2/develop/ref/javadoc/com/adobe/granite/taskmanagement/package-summary.html
 * - TaskManager: https://docs.adobe.com/docs/en/aem/6-2/develop/ref/javadoc/com/adobe/granite/taskmanagement/TaskManager.html
 * - Task: https://docs.adobe.com/docs/en/aem/6-2/develop/ref/javadoc/com/adobe/granite/taskmanagement/Task.html
 **/
@Component
@Service
public class SampleTaskManagementImpl implements SampleExecutor {
    private static final Logger log = LoggerFactory.getLogger(SampleTaskManagementImpl.class);

    private enum TASK_PRIORITY {
        High,
        Medium,
        Low
    }

    // ResourceResolverFactory just for illustrative purposes; you may be passing in your own ResourceResolver
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /**
     * Sample helper method that shows the 2 main ways to get
     *
     * @param resourceResolver the security context the TaskManager will operate under
     * @param path             the path where new Tasks will be created; if null the default Task location will be used (/etc/taskmanagement/tasks)
     * @return a TaskManager
     */
    private TaskManager getTaskManager(ResourceResolver resourceResolver, String path) {
        Resource tasksResource = resourceResolver.getResource(path);

        if (tasksResource != null) {
            // TaskManagers adapted from a Resource, will store their directs directly below this resource.
            // The most common use case is creating tasks under a project @ `<path-to-project>/jcr:content/tasks`
            return tasksResource.adaptTo(TaskManager.class);
        }

        // TaskManagers adapted from the ResourceResolver will store its tasks under /etc/taskmanagement/tasks
        // /etc/taskmanagement/tasks is OSGi configurable via com.adobe.granite.taskmanagement.impl.jcr.TaskStorageProvider along w the default archived tasks location (/etc/taskmanagement/archivedtasks)
        // These are non-project related tasks.

        // Note that a JCR Session may be used to adapt to a TaskManager as well to the same effect as the ResourceResolver method.
        return resourceResolver.adaptTo(TaskManager.class);
    }

    /**
     * Sample method that illustrates the basics of creating a Task
     */
    public void createTask() {
        ResourceResolver resourceResolver = null;

        try {
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(null);

            // See the above method for getting a TaskManager
            TaskManager taskManager = getTaskManager(resourceResolver, null);

            // Create new tasks using the taskManager's TaskmanagerFactory
            TaskManagerFactory taskManagerFactory = taskManager.getTaskManagerFactory();

            // Tasks can be created using the default type; It is best to use the Default type until instructions on how to properly create custom types is created.
            Task task = taskManagerFactory.newTask(Task.DEFAULT_TASK_TYPE);


            task.setName("This is the title of the task");
            task.setDescription("This is the description of the task");
            task.setInstructions("These are the instructions!");

            // Optionally set priority (High, Medium, Low)
            task.setProperty("taskPriority", TASK_PRIORITY.High.toString()); // or InboxItem.Priority.HIGH
            // Optionally set the start/due dates.
            task.setProperty("taskStartDate", new Date());
            task.setProperty("taskDueDate", new Date());

            // Set custom properties as well; note these will not display in the UI but can be used to make programmatic decisions
            task.setProperty("superCustomProperty", "superCustomValue");

            // Finally create the task; note this call will commit to the JCR.
            // The provided user context will be used to attempt the save, so user-permissions do come into play.
            // If no user context was provided, then a Task manager service user will be used.
            taskManager.createTask(task);

            // Do note that Tasks emit OSGi events that can be listened for and further acted upon.
            // https://docs.adobe.com/docs/en/aem/6-2/develop/ref/javadoc/com/adobe/granite/taskmanagement/TaskEvent.html#TASK_EVENT_TYPE

        } catch (TaskManagerException e) {
            log.error("Could not create task for [ {} ]", "xyz", e);
        } catch (LoginException e) {
            log.error("Could not get service user [ {} ]", "some-service-user", e);
        } finally {
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }

    private void update(TaskManager taskManager, String taskId) {
        // See createTask(..) above for how to get a TaskManager object

        try {
            // Get tasks via Task IDs
            Task task = taskManager.getTask(taskId);

            // Update the task using the setter methods!
            task.setProperty("someNewProperty", "Update the task as needed");

            // Persists the task changes to the JCR
            taskManager.saveTask(task);

        } catch (TaskManagerException e) {
            log.error("Could not save task for [ {} ]", taskId, e);
        }
    }

    private void otherActions(TaskManager taskManager, String taskId) {
        // See createTask(..) above for how to get a TaskManager object

        try {
            // Terminates the task
            taskManager.terminateTask(taskId);

            // Completes the task and saves the actionId to the task for future reference
            taskManager.completeTask(taskId, "complete");

            // Deletes the task
            taskManager.deleteTask(taskId);

        } catch (TaskManagerException e) {
            log.error("Could not save task for [ {} ]", taskId, e);
        }
    }

    public String execute() {
        return "I'm a Sample Task Management code harness";
    }
}
