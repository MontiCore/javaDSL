
<#assign retunType = cdPrinter.printType(ast.getMCReturnType())>

<#if retunType = "boolean">
     return false ;
<#elseif retunType = "int" || retunType = "double" || retunType = "long" || retunType = "short">
     return 0 ;
<#else >
     return null ;
</#if>

