<html>
<body>
<script>
  var $wnd = parent;
  var $doc = $wnd.document;
  var q = location.search.substring(1);
  var i = q.lastIndexOf("=");
  var $moduleName = q;
  var $moduleBaseURL = "<%= request.getContextPath()%>/" + @moduleBase;
  if (i != -1) {
  	$moduleBaseURL = q.substring(0, i);
  	$moduleName = q.substring(i+1);
  }
  parent.__gwt_initHostedModeModule(this, $moduleName);
</script>
</body>
</html>
