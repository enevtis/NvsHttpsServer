package pages.httpsserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import obj.httpsserver.nvs.com.TblField;
import util.httpsserver.nvs.com.SqlRequest;
import util.httpsserver.nvs.com.Util;

//flat_table?screen=app_systems&table=app_systems&mode=show
//flat_table?screen=settings&mode=show&table=user_settings&show_crypto=yes
//flat_table?screen=settings&mode=show&table=user_settings&show_crypto=yes

public class FlatTableHandler extends NvsHttpsServerHandlerTemplate {

	public HashMap<String, String> commonParams = null;
	private List<TblField> fields = null;
	private String screenName = "";
	private String tableName = "";
	public String caption = "";
	private String SQL = "";
	private int initialTop = 80;
	private boolean showCrypto = false;
	public Object gData = null;

	public FlatTableHandler(Object gData, Hashtable settings) {
		super(gData, settings);

	}

	public FlatTableHandler() {
	}

	public void getResponse(SSLSocket socket, String paramsString) {

		parseParams(paramsString);
		String resp = getPage();

		try {

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			out.write(header200());
			out.write("Content-Length: " + resp.getBytes("UTF-8").length + "\r\n");
			out.write("\r\n");
			out.write(resp);

			out.close();

		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.severe(errors.toString());
		}

	}

	public String getPage() {
		String out = "";
		out += getBeginPage();

		screenName = params.get("screen");
		String editMode = (params.containsKey("mode")) ? params.get("mode") : "READONLY";
		tableName = (params.containsKey("table")) ? params.get("table") : screenName;
		this.caption = (params.containsKey("caption")) ? params.get("caption") : "";

		if (params.containsKey("show_crypto")) {
			showCrypto = true;
			initialTop = 120;

		} else {
			showCrypto = false;
			initialTop = 80;
		}

		SQL = "select colLabel from data_struct_text where tableName='" + this.getClass().getSimpleName() + "' ";
		SQL += "and colName='" + screenName + "' and lang='" + settings.get("lang") + "'";

		if (!this.caption.isEmpty())
			caption = Util.decode(this.caption);
		else
			caption = select_from_DDFT_caption(screenName);

		out += strTopPanel(this.caption);

		SQL = select_from_DDFT_sql(screenName, tableName);

		SQL = replaceTemplates(SQL);
		
		this.fields = select_from_DDFT_fields(screenName); // flat_table_fields
		
		
		
		out += buildJavascriptRefreshFunction(SQL, tableName, fields, editMode.toUpperCase(), initialTop); 
		out += buildJavascriptAdditionalFunctionsArea(); 
		out += buildHtmlArea();

		if (showCrypto) { out += getCryptoAreaHtml(); 
			out += getCryptoAreaJavascript(); 
		}
		out += getEndPage();

		return out;
	}

	protected String replaceTemplates(String SQL_first) {
		String out = SQL_first;
		String paramName = "";

		paramName = "monitor_number";
		if (params.containsKey(paramName))
			out = SQL_first.replace("!MONITOR_NUMBER!", params.get(paramName));

		return out;
	}

	private String getCryptoAreaHtml() {
		String out = "";

		out += "<dev class='crypto'>";
		out += "<input id='input_crypto' type='password' class='crypto_ctrl' type='text' value='type here' > \n";
		out += "<input type='button' value='get hash' onclick='onCryptoButtonClick();'>\n";
		out += "<label id='label_crypto'></label>\n";
		out += "</dev>";
		return out;
	}

	private String getCryptoAreaJavascript() {
		String out = "";
		out += "<script>";
		out += "function onCryptoButtonClick(){";
		out += "console.log('onCryptoButtonClick');";
		out += "	var pass=document.getElementById('input_crypto').value;";
		out += "	var hash=getRequest('/free_request?action=get_hash&pass=' + pass );";
		out += "	document.getElementById('label_crypto').innerHTML=hash;";
		out += "}";
		out += "</script>";
		return out;
	}

	public String select_from_DDFT_sql(String screenName, String tableName) {
		String out = "";
		String SQL = "";

		SQL += "select `sql_text` from DDFT_sql where `screen` = '" + screenName + "'";

		if (!tableName.isEmpty() && !tableName.equals(screenName))
			SQL += " and `table`='" + tableName + "'";

		SQL += " order by id";

		List<Map<String, String>> records_list = sqlReq.getSelect(SQL);


				for (Map<String, String> rec : records_list) {

					out += rec.get("sql_text") + " ";

		}

		return out;
	}

	protected String select_from_DDFT_caption(String screenName) {
		String out = "";

		String SQL = "select * from DDFT_caption where screen='" + screenName + "' " + " and lang='"
				+ settings.get("lang") + "'";


		List<Map<String, String>> records_list = sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records_list) {

			out = rec.get("text");

		}

		return out;
	}

	protected List<TblField> select_from_DDFT_fields(String screenName) {

		List<TblField> out = new ArrayList();

		String SQL = "";

		SQL += "SELECT S1.*,  \n";
		SQL += "S1.colName AS Label  \n";
		SQL += "FROM  \n";
		SQL += "(SELECT * FROM DDFT_fields   \n";
		SQL += "WHERE    \n";
		SQL += "`screen` = '" + screenName + "'   \n";
		SQL += "AND `SHOW`='X') S1   \n";
		SQL += "ORDER BY S1.id;   \n";

		List<Map<String, String>> records_list = sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					TblField tf = new TblField();
					tf.fieldName = rec.get("colName");
					tf.fieldType = rec.get("colType");
					tf.fieldWidth = Integer.valueOf(rec.get("colLength"));

					tf.isChangeable = rec.get("editable").equals("Y") ? true : false;
					tf.refTable = rec.get("refTableName");
					tf.refKeyField = rec.get("refKeyColName");
					tf.refValueField = rec.get("refColValue");
					tf.refKeyFieldType = rec.get("refKeyColType");

					out.add(tf);
				}

			}
		}

		return out;

	}

	protected String buildJavascriptRefreshFunction(String SQL, String tableName, List<TblField> fields, String mode,
			int initTop) {
		String out = "";
		out += "<script>  \n";



		out += "window.tableData =  " + readTableData(SQL, fields) + ";  \n";
		out += "window.tableHeader =  " + getTableHeader(fields) + ";  \n";
		out += "window.tableTemplate =  " + getTableRowTemplate(fields) + ";  \n";
		out += "window.tableSelect =  null;  \n";
		out += "window.tableLeft =  40;  \n";
		out += "window.tableTop =  " + initTop + ";  \n";
		out += "window.currLeft =  0;  \n";
		out += "window.currTop =  0;  \n";
		out += "window.currRow =  0;  \n";
		out += "window.currCol =  0;  \n";
		out += "window.tableName = '" + tableName + "';  \n";
		out += "window.leftKoeff =  17;  \n";
		out += "window.tableLineHeight =  17;  \n";
		out += "window.selectedElement =  null;  \n";
		out += "window.mouseoverElement =  null;  \n";
		out += "window.curzIndex =  null;  \n";
		out += "window.mode =  '" + mode + "';  \n";
		out += "window.mouseLeft =  0;  \n";
		out += "window.mouseTop =  0;  \n";
		out += "window.propertiesItem=null; \n";


		out += "function refreshAll(){	 \n";
		out += " removeElementsByClass('table_data'); \n";
		out += " window.currLeft = window.tableLeft; \n";
		out += " window.currTop = window.tableTop; \n";

		out += " var tableWidth = 0; \n";
		out += "	for (var i=0; i < window.tableHeader.length; i++) { \n";
		out += " 		tableWidth += window.tableHeader[i].width * window.leftKoeff; \n";
		out += "  	} \n";
		out += " 	createDivForToolsCell(tableWidth - 3);  \n";

		out += " 	window.currTop += (window.tableLineHeight) + 2; \n";
		out += " 	window.currLeft = window.tableLeft; \n";
		out += "for (var i=0; i <window.tableHeader.length; i++) { \n";
		out += " 	createDivForHeaderCell(i); \n";
		out += "    window.currLeft += window.tableHeader[i].width * window.leftKoeff; \n";
		out += "  	} \n";
		out += "for (var i=0; i< window.tableData.length; i++) { \n";
		out += " 	window.currTop += (window.tableLineHeight) + 2; \n";
		out += " 	window.currLeft = window.tableLeft; \n";

		out += " 	var line = window.tableData[i]; \n";
		out += " 	var status = line[line.length-1]; \n";

		out += "	if(status!=='D') { \n";

		out += "		for (var j=0; j <window.tableHeader.length; j++) { \n";
		out += "			createDivForCell(i,j); \n";
		out += " 			window.currLeft += window.tableHeader[j].width * window.leftKoeff; \n";
		out += "  		} \n";
		out += "  	} \n";

		out += "  } \n";


		out += "document.addEventListener('contextmenu', onContextMenuPage, false); \n";
		out += "document.addEventListener('click', onClickDocument, false); \n";

		out += "} \n";

		out += "document.addEventListener('DOMContentLoaded', function(){ \n";
		out += "	refreshAll(); \n";
		out += "  }); \n";

		out += "</script>";

		return out;
	}

	protected String buildJavascriptAdditionalFunctionsArea() {
		String out = "";

		out += "<script>  \n";
		out += "function buildSelectList(cell){ \n";
		out += "var row=cell.getAttribute('row'); \n";
		out += "var col=cell.getAttribute('col'); \n";
		out += "var curKey=cell.innerHTML; \n";
		out += "window.selectedElement =  cell;  \n";
		out += "var refTable = tableHeader[col].table; \n";
		out += "var refKey = tableHeader[col].key; \n";
		out += "var refKeyType = tableHeader[col].key_type; \n";
		out += "var refValue = tableHeader[col].value; \n";
		out += "var params = '?refTable=' + refTable; \n";
		out += " params += '&refKey=' + refKey; \n";
		out += " params += '&refKeyType=' + refKeyType; \n";
		out += " params += '&refValue=' + refValue; \n";
		out += "var resp = getRequest('/json_sql'+params); \n";

		out += "console.log('params=' +params);  \n";
		out += "console.log('json=' +resp);  \n";

		out += "var el=document.getElementById('input_select'); \n";
		out += "document.body.appendChild(el); \n";
		out += "el.options.length = 0; \n";

		out += "var objJSON = JSON.parse(resp);  \n";

		out += "for (var i = 0; i < objJSON.length; ++i) {  \n";
		out += "	var line = objJSON[i];  \n";
		out += "	var opt = document.createElement('option');	 \n";
		out += "	opt.value = line.key; \n";
		out += "	opt.text = line.value; \n";
		out += "	el.appendChild(opt); \n";
		out += "}  \n";
		out += "	el.style.position = 'absolute'; \n";
		out += "	el.style.left = cell.getBoundingClientRect().left + 'px'; \n";
		out += "	el.style.top =  cell.getBoundingClientRect().top + 'px'; \n";
		out += "	el.style.display = 'block';  \n";
		out += "	for ( var i=0; i < el.length; i++ ) { \n";
		out += " 		if (el[i].value === curKey)	el[i].selected = true; \n";
		out += "	} \n";
		out += "} \n";

		out += "function onSelectInputChange(){ \n";
		out += " var cell=window.selectedElement;  \n";
		out += " cell.innerHTML =  document.getElementById('input_select').value;  \n";
		out += " var row=cell.getAttribute('row'); \n";
		out += " var col=cell.getAttribute('col'); \n";
		out += " var status = window.tableData[row][(window.tableHeader.length)]; \n";

		out += "	window.tableData[row][col] = document.getElementById('input_select').value; \n";
		out += " if (status==='A') { \n";
		out += "	window.tableData[row][(window.tableHeader.length)] = 'U'; \n";

		out += " } \n";

		out += " document.getElementById('input_select').style.display = 'none'; \n";
		out += " refreshAll();";
		out += "} \n";

		out += "function createDivForToolsCell(tableWidth){ \n";
		out += " var el = document.createElement('div'); \n";
		out += " el.className = 'table_data table_tools_panel'; \n";

		out += "if (window.mode === 'EDIT'){ \n";
		out += " el.innerHTML = '" + getTableToolsPanel() + "'; \n";
		out += "} \n";

		out += " el.style.position = 'absolute'; \n";
		out += " el.style.left = window.currLeft + 'px'; \n";
		out += " el.style.top =  window.currTop + 'px'; \n";
		out += " el.style.height = (window.tableLineHeight -2 )  + 'px'; \n";
		out += " el.style.width = tableWidth + 'px'; \n";
		out += " 	document.body.appendChild(el); \n";
		out += "} \n";

		out += "function createDivForHeaderCell(i){ \n";
		out += " var el = document.createElement('div'); \n";
		out += " el.className = 'table_data table_data_header'; \n";
		out += " el.innerHTML = window.tableHeader[i].name; \n";
		out += " el.style.position = 'absolute'; \n";
		out += " el.style.left = window.currLeft + 'px'; \n";
		out += " el.style.top =  window.currTop + 'px'; \n";
		out += " el.style.height = (window.tableLineHeight -2 )  + 'px'; \n";
		out += " el.style.width = ((window.tableHeader[i].width * window.leftKoeff) - 3) + 'px'; \n";
		out += " 	document.body.appendChild(el); \n";
		out += "} \n";

		out += "function createDivForCell(i,j){ \n";
		out += " var el = document.createElement('div'); \n";
		out += " el.className = 'table_data table_data_cell'; \n";
		out += " el.innerHTML = window.tableData[i][j]; \n";
		out += " el.style.position = 'absolute'; \n";
		out += " el.style.left = window.currLeft + 'px'; \n";
		out += " el.style.top =  window.currTop + 'px'; \n";
		out += " el.style.height = (window.tableLineHeight - 1 )+ 'px'; \n";
		out += " el.style.width = ((window.tableHeader[j].width * window.leftKoeff) - 3) + 'px'; \n";
		out += " el.setAttribute('row',i); \n";
		out += " el.setAttribute('col',j); \n";

		out += "	if(j == 0) { \n"; 

		out += "		el.addEventListener('contextmenu', onContextMenuKeyField, false); \n";
		out += "		el.addEventListener('dblclick', onDblClickCell); \n";
		out += "		el.addEventListener('click', onClickCell); \n";

		out += "	} else { \n";

		out += "		el.addEventListener('dblclick', onDblClickCell); \n";
		out += "		el.addEventListener('click', onClickCell); \n";

		out += "	} \n";

		out += " 	document.body.appendChild(el); \n";
		out += "} \n";

		out += "function onContextMenuKeyField(event) { \n";
		out += "if (window.mode === 'READONLY') return; \n";
		out += "if (window.mode === 'SHOW') return; \n";
		out += " window.selectedElement =  event.target;  \n";
		out += " window.mouseLeft = event.clientX + 'px';	 \n";
		out += " window.mouseTop = event.clientY + 'px';	 \n";
		out += " contextMenuBox = window.document.getElementById('context_menu_key_field');  \n";
		out += " contextMenuBox.style.left = event.clientX + 'px';	 \n";
		out += " contextMenuBox.style.top = event.clientY  + 'px';		 \n";
		out += " contextMenuBox.style.display = 'block';	 \n";
		out += "    event.preventDefault();   \n";
		out += "    event.stopPropagation();   \n";
		out += "} \n";

		out += "function onDblClickCell(event){ \n";
		out += "	var cell = event.target ;  \n";

		out += "	window.currRow = cell.getAttribute('row');  \n";
		out += "	window.currCol =  cell.getAttribute('col');  \n";
		out += "	if(window.tableHeader[window.currCol].is_changeable!=='y'){  \n";
		out += "		return; \n";
		out += "	} \n";

		out += " if(window.tableHeader[window.currCol].table.length<2){   \n";
		out += " 	box = window.document.getElementById('input_txt');  \n";
		out += " 	box.value = event.target.innerHTML ;	 \n";
		out += " 	rect = event.target.getBoundingClientRect();  \n";
		out += " 	box.style.left = (rect.left + window.pageXOffset) + 'px';	 \n";
		out += " 	box.style.top = (rect.top  + window.pageYOffset) + 'px';		 \n";
		out += " 	box.style.height = ( rect.bottom - rect.top - 4)   + 'px'; \n";
		out += " 	box.style.width = ( rect.right - rect.left - 8 )  + 'px'; \n";
		out += " 	box.style.display = 'block';	 \n";
		out += " 	box.style.zIndex = 10;	 \n";
		out += "} else { \n";

		out += "	console.log('cell=' + cell.innerHTML); \n";
		out += "	buildSelectList(cell); \n";
		out += "    	event.preventDefault();   \n";
		out += "    	event.stopPropagation();   \n";
		out += "} \n";

		out += "} \n";

		out += "function onClickCell(event){ \n";
		out += " 	document.getElementById('input_txt').style.display = 'none';  \n";
		out += "	document.getElementById('input_select').style.display = 'none'; \n";

		out += "} \n";

		out += "function onInputChange(){ \n";
		out += " box = window.document.getElementById('input_txt');  \n";
		out += " line = window.tableData[window.currRow];  \n";
		out += " window.tableData[window.currRow][window.currCol] = box.value;   \n";
		out += " var status = line[line.length-1];   \n";
		out += " if(status==='A'){   \n";
		out += " 	line[line.length-1] = 'U';   \n";
		out += " }   \n";

		out += " box.style.display = 'none';	 \n";
		out += " 	refreshAll();   \n";
		out += "} \n";

		out += "function saveAll(){	 \n";
		out += " var strSQL = ''; \n";
		out += "for (var i=0; i <window.tableData.length; i++) { \n";
		out += "var line= window.tableData[i]; \n";
		out += "var status= line[line.length-1]; \n";
		out += "if(status === 'U') { \n"; ////////// Update /////////
		out += "strSQL += 'update ' + window.tableName + ' set '; \n";
		out += "	for (var j=0; j < line.length-1; j++) { \n";
		out += "		if(window.tableHeader[j].is_changeable === 'y') { \n";
		out += "			if(window.tableHeader[j].type === 'string') { \n";
		out += "				strSQL += '\\`' + window.tableHeader[j].name + '\\`=\"' + line[j] + '\",'; \n";
		out += "			} else { \n";
		out += "				strSQL += '\\`' + window.tableHeader[j].name + '\\`=' + line[j] + ','; \n";
		out += "			} \n";
		out += "		} \n";
		out += "	} \n";
		out += "	strSQL = strSQL.slice(0, -1); \n";
		out += "	if(window.tableHeader[0].type === 'string') { \n";
		out += "	 	strSQL += ' where \\`' + window.tableHeader[0].name + '\\`=\"' + line[0] + '\"'; \n";
		out += " 	} else { \n";
		out += "	 	strSQL += ' where \\`' + window.tableHeader[0].name + '\\`=' + line[0] + ''; \n";
		out += " 	} \n";
		out += "	strSQL += ';'; \n";
		out += " } else if (status === 'D') {\n"; ////// Delete
		out += "	strSQL += 'delete from ' + window.tableName ; \n";
		out += "	if(window.tableHeader[0].type === 'string') { \n";
		out += "	 	strSQL += ' where \\`' + window.tableHeader[0].name + '\\`=\"' + line[0] + '\"'; \n";
		out += " 	} else { \n";
		out += "	 	strSQL += ' where \\`' + window.tableHeader[0].name + '\\`=' + line[0] + ''; \n";
		out += " 	} \n";
		out += "	strSQL += ';'; \n";
		out += " } else if (status === 'I') {\n"; ////// Insert
		out += "	strSQL += 'insert into ' + window.tableName ; \n";
		out += "	var fieldList =''; \n";
		out += "	var valueList =''; \n";
		out += "	for (var j=0; j < line.length-1; j++) { \n";
		out += "		if(window.tableHeader[j].is_changeable === 'y') { \n";
		out += "			fieldList +='\\`' + window.tableHeader[j].name + '\\`,'; \n";
		out += "			if(window.tableHeader[j].type === 'string') { \n";
		out += "				valueList += '\"' +  line[j] + '\",'; \n";
		out += " 			} else { \n";
		out += "				valueList +=  line[j] + ','; \n";
		out += " 			} \n";
		out += " 		} \n";
		out += " } \n";
		out += " fieldList = fieldList.slice(0, -1); \n";
		out += " valueList = valueList.slice(0, -1); \n";
		out += "	strSQL += ' (' + fieldList + ') values (' + valueList +')' ; \n";
		out += "	strSQL += ';'; \n";
		out += "} \n";
		out += "} \n";
		out += "strSQL = strSQL.slice(0, -1); \n";
		out += "console.log(strSQL); \n";
		out += " postRequest('/sql_save', strSQL); \n";
		out += " console.log('sql_save executed'); \n";
		out += " sleep(1000); \n";
		out += " location.reload(); \n";
		out += " return false; \n";
		out += "} \n";


		out += "function sleep(delay) { \n";
		out += " var start = new Date().getTime(); \n";
		out += " while (new Date().getTime() < start + delay); \n";
		out += "} \n";


		out += "function onDeleteRow() { \n";
		out += "if(!window.selectedElement) { \n";
		out += "	alert('double click on row for delete before');  \n";
		out += " return; \n";
		out += "} \n";
		out += " for(var i=0; i < tableData.length; i++) { \n";
		out += "    var line =  tableData[i]; \n";
		out += "	if(line[0]===window.selectedElement.innerHTML) { \n";
		out += "      		line[line.length-1] = 'D' \n";
		out += "  	}	\n";
		out += "   }	\n";
		out += "	refreshAll();  \n";
		out += "  }	\n";

		out += "function onAddNewRow() { \n";
		out += " var newItem = Object.create(window.tableTemplate); \n";
		out += " for(var i=0; i<window.tableTemplate.length; i++){ \n";
		out += "   newItem[i]=window.tableTemplate[i]; \n";
		out += "  }	\n";
		out += " window.tableData.push(newItem); \n";
		out += "  console.log('onAddNewRow' + newItem[newItem.lenght-1]); \n";
		out += "	refreshAll();  \n";
		out += "  }	\n";

		out += "function onSwitchToEditMode() { \n";
		out += "if (window.mode === 'READONLY') return; \n";
		out += "	window.mode =  'EDIT';  \n";

		out += "	refreshAll();  \n";
		out += "} \n";

		out += "function onContextMenuPage(event) { \n";
		out += "if (document.addEventListener) { \n";
		out += " if (window.mode=='EDIT') return; \n";
		out += " 	contextMenuBoxPage = window.document.getElementById('context_page_menu_show');  \n";
		out += " 	contextMenuBoxPage.style.left = event.clientX + 'px';	 \n";
		out += " 	contextMenuBoxPage.style.top = event.clientY  + 'px';		 \n";
		out += " 	contextMenuBoxPage.style.display = 'block';	 \n";
		out += "    event.preventDefault();   \n";
		out += "} else {    \n";
		out += "	document.attachEvent('oncontextmenu', function() {    \n";
		out += "	window.event.returnValue = false;    \n";
		out += "});    \n";
		out += "} \n";
		out += "} \n";

		out += "function onClickDocument(event) { \n";
		out += " document.getElementById('context_page_menu_show').style.display = 'none'; \n";
		out += " document.getElementById('context_menu_key_field').style.display = 'none'; \n";
		out += "} \n";

		out += "function postRequest(url, text, callback) { \n";
		out += " var xhr = new XMLHttpRequest(); \n";
		out += " xhr.open('POST', url, true); \n";
		out += " xhr.responseType = 'text' \n";
		out += " xhr.setRequestHeader('Content-Type', 'text/plain'); \n";
		out += " xhr.onload = function(e) {  \n";
		out += " if (this.status == 200) { \n";
		out += "     console.log(this.responseText); \n";
		out += " if(callback) callback(statuses); \n";
		out += "  }	\n";
		out += " };	 \n";
		out += " xhr.send(text);  \n";
		out += "} \n";

		out += "function getRequest(url, callback) { \n";
		out += " var xhr = new XMLHttpRequest(); \n";
		out += " xhr.open('GET', url, false); \n";
		out += " xhr.send();  \n";

		out += " xhr.onreadystatechange = function() {  \n";
		out += " 	if (this.readyState == 4 && this.status == 200) { \n";
		out += "   		var response = this.responseText; \n";
		out += " 		if(callback) callback(response); \n";
		out += "  	}	\n";
		out += " };	 \n";
		out += " return xhr.responseText;	 \n";
		out += "} \n";

		out += "</script>\n";
		return out;
	}
	protected String readTableData(String SQL, List<TblField> fields) {

		String out = "";
		String buffer = "";

		List<Map<String, String>> records_list = sqlReq.getSelect(SQL);

		out += "[";

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					out += "[";

					for (TblField field : fields) {

						buffer = rec.get(field.fieldName);
						buffer = (buffer == null) ? ""
								: buffer.replaceAll("[^A-Za-z0-9_. :/\\-<>?=&@\\p{IsCyrillic}]", "");

						out += "\"" + buffer + "\",";
					}

					out += "\"A\","; 

					out = out.substring(0, out.length() - 1);
					out += "],";
				}
				out = out.substring(0, out.length() - 1);

			}
		}

		out += "]";

		return out;

	}
	protected String getTableToolsPanel() {
		String out = "";
		String style = "cursor: pointer;";
		out += "<img src=\"/img/save.png\" onclick=\"saveAll();\" title=\"save\" style=\"" + style + "\">";
		out += "&nbsp;<img  src=\"/img/add.png\" onclick=\"onAddNewRow();\" title=\"insert row\" style=\"" + style + "\">";
		return out;
	}

	protected String getTableHeader(List<TblField> fields) {
		String out = "";
		out += "[";

		for (TblField tf : fields) {
			out += "{";
			out += "\"name\":\"" + tf.fieldName + "\",";
			out += "\"type\":\"" + tf.fieldType + "\",";
			out += "\"table\":\"" + tf.refTable + "\",";
			out += "\"key\":\"" + tf.refKeyField + "\",";
			out += "\"key_type\":\"" + tf.refKeyFieldType + "\",";
			out += "\"value\":\"" + tf.refValueField + "\",";
			out += "\"is_changeable\":\"" + (tf.isChangeable ? "y" : "n") + "\",";
			out += "\"width\":\"" + tf.fieldWidth + "\"";
			out += "},";
		}
		out = out.substring(0, out.length() - 1);
		out += "]";
		return out;
	}
	protected String getTableRowTemplate(List<TblField> fields) {
		String out = "";
		String buffer = "";

		out += "[";

		for (TblField tf : fields) {

			buffer = (tf.defaultValue != null && !tf.defaultValue.isEmpty()) ? tf.defaultValue : "";

			switch (tf.fieldType) {
			case "int":
				buffer = (tf.defaultValue != null && !tf.defaultValue.isEmpty()) ? tf.defaultValue : "0";
				out += "\"" + buffer + "\",";
				break;
			case "string":
				buffer = (tf.defaultValue != null && !tf.defaultValue.isEmpty()) ? tf.defaultValue : "";
				out += "\"" + tf.defaultValue + "\",";
				break;
			case "datetime":

				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				buffer = (tf.defaultValue != null && !tf.defaultValue.isEmpty()) ? tf.defaultValue : timeStamp;
				out += "\"" + buffer + "\",";
				break;

			default:
				out += "\"\",";

			}

		}
		out += "\"I\"";

//		out = out.substring(0, out.length() - 1);

		out += "]";
		return out;
	}
	protected String buildHtmlArea() {
		String out = "";
		String function = "";

		/// ************ контекстное меню для SHOW режима ************ ///////
		out += "<div class='menu' id='context_page_menu_show'>  \n";
		function = "onSwitchToEditMode";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">"
				+ "Switch to EDIT mode" + "</div>  \n";
		out += "</div>  \n";

		/// ************ контекстное меню для Ключевого поля ************ ///////
		out += "<div class='menu' id='context_menu_key_field'>  \n";

		function = "onDeleteRow";
		out += "<div id='" + function + "' class='menu-item' onclick=\"" + function + "();\">" + "Delete row"
				+ "</div>  \n";

		function = "onAddNewRow";
		out += "<div id='" + function + "' class='menu-item' onclick=\"" + function + "();\">" + "Add new row"
				+ "</div>  \n";

		function = "saveAll";
		out += "<div id='" + function + "' class='menu-item' onclick=\"" + function + "();\">" + "Save"
				+ "</div>  \n";
		out += "</div>  \n";

		out += "</div>  \n";

		out += " <input id='input_txt' type='text' class='input_ctrl' type='text' size='7' onchange='onInputChange();'> \n";
		out += " <select id='input_select' class='input_ctrl input_select' onchange='onSelectInputChange(this);'></select> \n";

//		out += " <input type='button' style='position:absolute;left:10px;top:600px' onclick='onClickButton();' value='ok'> \n";
		return out;
	}
}
