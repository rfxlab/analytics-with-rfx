<!DOCTYPE html>
<html>
<head></head>
<body>

	<h3>Rfx Http Server version {{rfxServerVersion}}</h3>
	
	<b>
	{{#doIf rfxServerVersion '==' "1.0" }}
		 Stable Build
	{{else}}	  
		 Snapshot Build
	{{/doIf}}
	</b>
	
	<h3>Time: {{time}}</h3>
	
	<strong>
	
		{{#if showAll}}
		  All System JVM information
		{{/if}}
		
		{{#if showCompact}}
		  Important JVM information	
		{{/if}}
	
	</strong>	
	
	{{#ifHasData memoryStats}}
	<br>
	<strong>Memory:	{{memoryStats}}	</strong>
	{{/ifHasData}}
	
	<ul>
		{{#each infos}}
			<li>{{.}}</li> 
		{{/each}}
	</ul>	
	
	{{#base64Decode "SmF2YSA4IGlzIGNvb2wgcHJvZ3JhbW1pbmcgbGFuZ3VhZ2U=" }}{{/base64Decode}}	
	{{#randomInteger}}{{/randomInteger}}
	
</body>
</html>