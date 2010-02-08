package org.obiba.wicket.markup.html.table;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.Resource;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;
import org.obiba.core.domain.IEntity;
import org.obiba.core.service.EntityQueryService;
import org.obiba.wicket.markup.html.ResourceGetter;
import org.obiba.wicket.markup.html.panel.ImageLabelLinkPanel;
import org.obiba.wicket.util.resource.CsvResourceStream;


public class EntityListTablePanel<T> extends Panel {

  private static final long serialVersionUID = -5163898654558983434L;

  public static final int DEFAULT_ROWS_PER_PAGE = 100;

  private Boolean allSelected = false;

  private Map<Serializable, EntitySelection> selections = new HashMap<Serializable, EntitySelection>();

  private IColumnProvider<T> columnProvider;
  private IColumnProvider<T> csvColumnProvider;

  private SortableDataProvider<T> dataProvider;

  private AjaxDataTable<T> dataTable;
  
  private ColumnSelectorPanel<T> selector;

  private RowSelectionColumn<T> rowSelectionColumn;

  private boolean displayRowSelectionColumn = false;

  /**
   * Constructor with the default title, rows per page and {@link SortableDataProviderEntityServiceImpl}. 
   * @param id
   * @param service
   * @param columns
   */
  public EntityListTablePanel(String id, EntityQueryService queryService, Class<T> type, IColumnProvider<T> columns) {
    this(id, queryService, type, columns, new Model<String>(type.getSimpleName()), DEFAULT_ROWS_PER_PAGE);
  }

  /**
   * Constructor with the default rows per page and {@link SortableDataProviderEntityServiceImpl}. 
   * @param id
   * @param service
   * @param columns
   * @param entityNameModel
   */
  public EntityListTablePanel(String id, EntityQueryService queryService, Class<T> type, IColumnProvider<T> columns, IModel<String> entityNameModel) {
    this(id, queryService, type, columns, entityNameModel, DEFAULT_ROWS_PER_PAGE);
  }

  /**
   * Constructor using a {@link SortableDataProviderEntityServiceImpl}.
   * @param id
   * @param service
   * @param columns
   * @param entityNameModel
   * @param rowsPerPage
   */
  public EntityListTablePanel(String id, EntityQueryService queryService, Class<T> type, IColumnProvider<T> columns, IModel<String> entityNameModel, int rowsPerPage) {
    super(id);
    internalConstruct(new SortableDataProviderEntityServiceImpl<T>(queryService, type), columns, entityNameModel, rowsPerPage);
  }

  /**
   * Constructor with the default title, rows per page and {@link FilteredSortableDataProviderEntityServiceImpl}. 
   * @param id
   * @param service
   * @param template
   * @param columns
   */
  public EntityListTablePanel(String id, EntityQueryService queryService, T template, IColumnProvider<T> columns) {
    this(id, queryService, template, columns, new Model<String>(template.getClass().getSimpleName()), DEFAULT_ROWS_PER_PAGE);
  }

  /**
   * Constructor with the default rows per page and {@link FilteredSortableDataProviderEntityServiceImpl}. 
   * @param id
   * @param service
   * @param template
   * @param columns
   * @param entityNameModel
   */
  public EntityListTablePanel(String id, EntityQueryService queryService, T template, IColumnProvider<T> columns, IModel<String> entityNameModel) {
    this(id, queryService, template, columns, entityNameModel, DEFAULT_ROWS_PER_PAGE);
  }

  /**
   * Constructor using a {@link FilteredSortableDataProviderEntityServiceImpl}.
   * @param id
   * @param service
   * @param template
   * @param columns
   * @param entityNameModel
   * @param rowsPerPage
   */
  public EntityListTablePanel(String id, EntityQueryService queryService, T template, IColumnProvider<T> columns, IModel<String> entityNameModel, int rowsPerPage) {
    super(id);
    internalConstruct(new FilteredSortableDataProviderEntityServiceImpl<T>(queryService, template), columns, entityNameModel, rowsPerPage);
  }

  /**
   * Main constructor.
   * @param id
   * @param service
   * @param dataProvider
   * @param columns
   * @param entityNameModel
   * @param rowsPerPage
   */
  public EntityListTablePanel(String id, SortableDataProvider<T> dataProvider, IColumnProvider<T> columns, IModel<String> entityNameModel, int rowsPerPage) {
    super(id);
    internalConstruct(dataProvider, columns, entityNameModel, rowsPerPage);
  }

  private void internalConstruct(SortableDataProvider<T> dataProvider, IColumnProvider<T> columns, IModel<String> entityNameModel, int rowsPerPage) {
    setOutputMarkupId(true);
    this.columnProvider = columns;
    this.dataProvider = dataProvider;

    List<IColumn<T>> displayableColumns = columnProvider.getDefaultColumns();
    selector = new ColumnSelectorPanel<T>("commands", this);
    add(this.dataTable = new AjaxDataTable<T>("list", entityNameModel, displayableColumns, dataProvider, selector, rowsPerPage) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onPageChanged() {
        super.onPageChanged();
        EntityListTablePanel.this.onPageChanged();
      }
    });
  }
  
  /**
   * Sets the item reuse strategy. This strategy controls the creation of {@link Item}s.
   * 
   * @see RefreshingView#setItemReuseStrategy(IItemReuseStrategy)
   * @see IItemReuseStrategy
   * 
   * @param strategy
   *            item reuse strategy
   * @return this for chaining
   */
  public final EntityListTablePanel<T> setItemReuseStrategy(IItemReuseStrategy strategy)
  {
    dataTable.setItemReuseStrategy(strategy);
    return this;
  }

  public IColumnProvider<T> getColumnProvider() {
    return columnProvider;
  }

  @SuppressWarnings("unchecked")
  void updateColumns(List<IColumn<T>> columns, AjaxRequestTarget target) {
    if(this.displayRowSelectionColumn == true) {
      if(this.rowSelectionColumn == null) {
        this.rowSelectionColumn = new RowSelectionColumn(this);
      }
      columns = new ArrayList<IColumn<T>>(columns);
      columns.add(0, this.rowSelectionColumn);
    }

    List<IBehavior> behaviours = this.dataTable.getBehaviors();
    int currentPage = this.dataTable.getCurrentPage();
    this.dataTable = new AjaxDataTable<T>("list", this.dataTable.getTitleModel(), columns, dataProvider, selector, this.dataTable.getRowsPerPage()) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onPageChanged() {
        super.onPageChanged();
        EntityListTablePanel.this.onPageChanged();
      }
    };
    if(behaviours != null) {
      for(IBehavior behaviour : behaviours) {
        this.dataTable.add(behaviour);
      }
    }
    this.dataTable.setCurrentPage(currentPage);
    super.replace(this.dataTable);
    if(target != null) {
      target.addComponent(this);
    }
  }

  /**
   * Default behaviour is to clean the selections made on previous page.
   *
   */
  protected void onPageChanged() {
    clearSelections();
  }
  
  @Override
  protected void onModelChanged() {
    clearSelections();
    super.onModelChanged();
  }
  
  /**
   * Clear the selections and the select-all flag.
   *
   */
  public void clearSelections() {
    getSelections().clear();
    setAllSelected(false);
    if (rowSelectionColumn != null)
      rowSelectionColumn.clearSelectionComponents();
  }
  
  
  /**
   * Sets whether to display the column selection widget on the table's header row.
   * @param selection true if the widget should be displayed.
   */
  public void setAllowColumnSelection(boolean selection) {
    if(selection == false) {
      selector.setVisible(false);
    } else {
      selector.setVisible(true);
    }
  }

  public void setDisplayRowSelectionColumn(boolean displayRowSelectionColumn) {
    this.displayRowSelectionColumn = displayRowSelectionColumn;
    updateColumns(columnProvider.getDefaultColumns(), null);
  }

  /**
   * Override this method to set your own command components in a panel.
   * @param panelId
   * @return
   */
  public Panel getCommandPanel(String panelId) {
    return new EmptyPanel(panelId);
  }
  
  public Panel getDefaultCommandPanel(String panelId) {
    return getExportCommandPanel(panelId, ResourceGetter.getImage("document_out.gif"), new Model<String>("Export "));
  }
  
  /**
   * Get the command that export table content to a csv file, with the given image.
   * @param panelId
   * @param image
   * @return
   */
  public Panel getExportCommandPanel(String panelId, Resource image, IModel<String> linkLabel) {
    ImageLabelLinkPanel panel = new ImageLabelLinkPanel(panelId, image, linkLabel, ImageLabelLinkPanel.ImageLocation.left) {
      private static final long serialVersionUID = 8347134007933182401L;

      @Override
      public void onClick() {
        getRequestCycle().setRequestTarget(new ResourceStreamRequestTarget(getReportStream()) {
          @Override
          public String getFileName() {
            return getCsvFileName();
          }
        });
      }
      
    };
    
    return panel;
  }
  
  
  /**
   * Returns the filename of the generated CSV file. 
   * @return
   */
  public String getCsvFileName() {
    SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd_HHmm");
    String name = formater.format(new Date());
    
    String header;
    //Supporting the old way of defining the filename for a CSV.
    Component entityNameComponent = EntityListTablePanel.this.get("entityName");
    if (entityNameComponent != null) {
      header = entityNameComponent.getDefaultModelObjectAsString();              
    }
    else {
      header = EntityListTablePanel.this.dataTable.getTitleModel().toString();              
    }
    
    name = "_" + header;
    name = name.replace(' ', '_');
    
    return name + "." + CsvResourceStream.FILE_SUFFIX;
  }
  
  
  /**
   * Get the stream of list data in a csv file.
   * @return
   */
  public IResourceStream getReportStream() {
    CsvResourceStream csv = getCsvResourceStream();
    
    List<IColumn<T>> columns = columnProvider.getDefaultColumns();
    if (csvColumnProvider != null) {
      columns = csvColumnProvider.getDefaultColumns();
    }
    else {
      columns = columnProvider.getDefaultColumns();
    }
    
    
    for (String name : getColumnHeaderNames(columns)) {
      csv.append(name);
    }
    csv.appendLine();
    
    SortableDataProvider<T> dataProvider = this.dataProvider;
    
    int size = dataProvider.size();
    int from = 0;
    int count = (DEFAULT_ROWS_PER_PAGE > size) ? size : DEFAULT_ROWS_PER_PAGE;
    int idx = 0;
    while (from < size) {
      Iterator<? extends T> iter = dataProvider.iterator(from, count);
      while (iter.hasNext()) {
        IModel<T> model = dataProvider.model(iter.next());
        int pos=0;
        for (IColumn<T> col : columns) {
          if (!(col instanceof HeaderlessColumn<?>)) {
            Item<ICellPopulator<T>> cellItem = new Item<ICellPopulator<T>>("dummy",idx,null);
            col.populateItem(cellItem, "dummy", model);
            Component comp = cellItem.get("dummy");
            
            String value = "";
            if (comp != null)
              value  = comp.getDefaultModelObjectAsString();
            
            csv.append(value);
            pos++;
          }
        }
        idx++;
        csv.appendLine();
      }
      from = idx;
      if ((from + DEFAULT_ROWS_PER_PAGE) > size) {
        count = size - from;
      }
      else {
        count = from + DEFAULT_ROWS_PER_PAGE;
      }
    }
    csv.appendEnd();
    
    return csv;
  }
  
  /**
   * Gets the CsvResourceStream instance used to stream the CSV document. It is protected to allow overriding, for example to change the value separating character of the resulting CSV file.
   * @return
   */
  protected CsvResourceStream getCsvResourceStream() {
    return new CsvResourceStream();
  }


  /**
   * Get the (localized) column header names. Ignore headerless columns.
   * @param columns
   * @return
   */
  protected List<String> getColumnHeaderNames(List<IColumn<T>> columns) {
    List<String> names = new ArrayList<String>();
    
    for (IColumn<T> col : columns) {
      if (col instanceof AbstractColumn<?> && !(col instanceof HeaderlessColumn<?>))
        names.add(getColumnHeaderName(col));
    }
    
    return names;
  }

  /**
   * Get the (localized) column header name from column display model.
   * @param column
   * @return
   */
  String getColumnHeaderName(IColumn<T> column) {
    String n = null;

    if(column instanceof AbstractColumn<?> && column instanceof HeaderlessColumn<?> == false) {
      IModel<String> displayModel = ((AbstractColumn<T>)column).getDisplayModel();
      if(displayModel != null) {
        if (displayModel instanceof StringResourceModel) {
          n = ((StringResourceModel)displayModel).getString();
        } else if(displayModel.getObject() != null){
          n = displayModel.getObject().toString();
        }
      }
    }
    return n;
  }

  public void setCellSpacing(int value) {
    for (IBehavior behaviour : (List<IBehavior>)dataTable.getBehaviors()) {
      if(behaviour instanceof SimpleAttributeModifier) {
        SimpleAttributeModifier modifier = (SimpleAttributeModifier)behaviour;
        if(modifier.getAttribute().equalsIgnoreCase("cellspacing")) {
          dataTable.remove(behaviour);
          break;
        }
      }
    }
    dataTable.add(new SimpleAttributeModifier("cellspacing", Integer.toString(value)));
  }

  public boolean isAllSelected() {
    if (allSelected == null)
      return false;
    else
      return allSelected;
  }

  public Boolean getAllSelected() {
    return allSelected;
  }

  public void setAllSelected(Boolean allSelected) {
    this.allSelected = allSelected;
  }

  /**
   * Get a map with entity ids and their selection.
   * @return
   */
  public Map<Serializable, EntitySelection> getSelections() {
    return selections;
  }

  /**
   * Get the selection object for entity model. Create it if needed.
   * @param model
   * @return
   */
  protected EntitySelection getSelection(IModel<T> model) {
    if (model == null)
      return null;
    
    EntitySelection selection;
    IEntity entity = (IEntity)model.getObject();
    if (selections.containsKey(entity.getId())) {
      selection = selections.get(entity.getId());
    }
    else {
      selection = new EntitySelection(model);
      selections.put(selection.getEntityId(), selection);
    }

    return selection;
  }
  
  
  /**
   * Sets the checkbox of the row corresponding to the given model to "selected".
   * @param model the targeted row's model
   */
  public void setSelected(IModel<T> model) {
    EntitySelection es = getSelection(model);
    es.setSelected(true);
  }
  
  
  /**
   * Sets the checkbox of the row corresponding to the given model to "deselected".
   * @param model the targeted row's model
   */
  public void setDeselected(IModel<T> model) {
    EntitySelection es = getSelection(model);
    es.setSelected(false);
  }
  

  /**
   * Get the ids that where selected in the selection column.
   * @return
   */
  public List<IEntity> getSelectedEntities() {
    List<IEntity> selected = new ArrayList<IEntity>();
    for(Map.Entry<Serializable, EntitySelection> entry : selections.entrySet()) {
      EntitySelection selection = entry.getValue();
      if(selection.getSelected()) {
        selected.add(selection.getEntity());
      }
    }
    return selected;
  }

  public void setPageSize(int rowsPerPage) {
    dataTable.setRowsPerPage(rowsPerPage);
  }

  public Integer getCount() {
    return dataProvider.size();
  }

  /**
   * Stores the id of the entity and whether it is selected or not.
   */
  protected class EntitySelection implements IClusterable {

    private static final long serialVersionUID = 1L;

    private IModel<T> model;
    private boolean selected = false;

    public EntitySelection(IModel<T> model, boolean selected) {
      this.model = model;
      this.selected = selected;
    }

    public EntitySelection(IModel<T> model) {
      this(model, allSelected);
    }

    public IModel<T> getModel() {
      return model;
    }
    
    public Serializable getEntityId() {
      return getEntity().getId();
    }
    
    public IEntity getEntity() {
      return (IEntity)model.getObject();
    }

    public boolean getSelected() {
      return selected;
    }

    public void setSelected(boolean selected) {
      this.selected = selected;
    }
    
  }

  public IColumnProvider<T> getCsvColumnProvider() {
    return csvColumnProvider;
  }

  public void setCsvColumnProvider(IColumnProvider<T> csvColumnProvider) {
    this.csvColumnProvider = csvColumnProvider;
  }
  
  protected AjaxDataTable<T> getDataTable(){
	  return dataTable;
  }
  
}
