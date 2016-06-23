package com.neueda.jetbrains.plugin.graphdb.jetbrains.ui.console;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.JBTabsPaneImpl;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBus;
import com.neueda.jetbrains.plugin.graphdb.jetbrains.GraphConstants;
import com.neueda.jetbrains.plugin.graphdb.jetbrains.ui.console.graph.GraphPanel;
import com.neueda.jetbrains.plugin.graphdb.jetbrains.ui.console.status.ExecutionStatusBarWidget;
import com.neueda.jetbrains.plugin.graphdb.jetbrains.ui.console.table.TablePanel;
import com.neueda.jetbrains.plugin.graphdb.visualization.services.LookAndFeelService;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class ConsoleToolWindow implements ToolWindowFactory, Disposable {

    private ExecutionStatusBarWidget executionStatusBarWidget;
    private JPanel consoleToolWindowContent;
    private JBTabsPaneImpl consoleTabsPane;
    private JBTabsImpl consoleTabs;

    // Graph
    private JPanel consoleToolbarPanel;
    private JPanel graphCanvas;
    private JBScrollPane entityDetailsScrollPane;
    private Tree entityDetailsTree;

    // Table
    private JBScrollPane tableScrollPane;
    private JBTable tableExecuteResults;
    private JPanel entityDetailsScrollContent;
    private JPanel logTab;
    private JPanel graphTab;
    private JBTabbedPane defaultTabContainer;
    private JBSplitter graphSplitter;

    private LookAndFeelService lookAndFeelService;

    private TablePanel tablePanel;
    private GraphPanel graphPanel;

    public ConsoleToolWindow() {
        tablePanel = new TablePanel();
        graphPanel = new GraphPanel();
        lookAndFeelService = ServiceManager.getService(LookAndFeelService.class);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Bootstrap tool window
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(consoleToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);

        updateLookAndFeel();
        initializeWidgets(project);
        initializeUiComponents(project.getMessageBus());

        // Hide standard tabs
        defaultTabContainer.setVisible(false);

        // Tabs
        consoleTabs.setFirstTabOffset(0);
        consoleTabs.addTab(new TabInfo(logTab)
                .setText("Log"));
        consoleTabs.addTab(new TabInfo(graphTab)
                .setText("Graph"));
        consoleTabs.addTab(new TabInfo(tableScrollPane)
                .setText("Table"));
        consoleTabs.setSelectionChangeHandler((info, requestFocus, doChangeSelection) -> {
            ActionCallback callback = doChangeSelection.run();
            graphPanel.resetPan();
            return callback;
        });

        // Actions
        final ActionGroup consoleActionGroup = (ActionGroup)
                ActionManager.getInstance().getAction(GraphConstants.Actions.CONSOLE_ACTIONS);
        ActionToolbar consoleToolbar = ActionManager.getInstance()
                .createActionToolbar(GraphConstants.ToolWindow.CONSOLE_TOOL_WINDOW, consoleActionGroup, false);
        consoleToolbarPanel.add(consoleToolbar.getComponent(), BorderLayout.CENTER);
        consoleToolbarPanel.setBorder(new CustomLineBorder(0, 0, 0, 1));
        consoleToolbarPanel.validate();
    }

    private void createUIComponents() {
        graphCanvas = new JPanel(new GridLayout(0, 1));
        consoleTabsPane = new JBTabsPaneImpl(null, SwingConstants.TOP, this);
        consoleTabs = (JBTabsImpl) consoleTabsPane.getTabs();
    }

    private void updateLookAndFeel() {
        tableScrollPane.setBorder(IdeBorderFactory.createEmptyBorder());
        entityDetailsScrollPane.setBorder(IdeBorderFactory.createEmptyBorder());
    }

    private void initializeUiComponents(MessageBus messageBus) {
        graphPanel.initialize(this, messageBus);
        tablePanel.initialize(this, messageBus);
    }

    private void initializeWidgets(Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        executionStatusBarWidget = new ExecutionStatusBarWidget(project.getMessageBus());
        statusBar.addWidget(executionStatusBarWidget, "before Position");
    }

    public TablePanel getTablePanel() {
        return tablePanel;
    }

    public GraphPanel getGraphPanel() {
        return graphPanel;
    }

    public LookAndFeelService getLookAndFeelService() {
        return lookAndFeelService;
    }

    public JPanel getGraphCanvas() {
        return graphCanvas;
    }

    public Tree getEntityDetailsTree() {
        return entityDetailsTree;
    }

    public JBTable getTableExecuteResults() {
        return tableExecuteResults;
    }

    @Override
    public void dispose() {
    }
}
