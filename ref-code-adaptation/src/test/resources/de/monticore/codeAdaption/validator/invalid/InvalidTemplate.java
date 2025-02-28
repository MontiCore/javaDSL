import de.monticore.codeAdaption.utils.Adapt;

@Adapt(template = "${}",ref = "Entity")
public class InvalidTemplate {
    @Adapt(template = "${}",ref = "Entity")
    public String someAttribut ;

    @Adapt(template = "${}",ref = "Entity")
    public  void someMethod(@Adapt(template = "${}",ref = "Entity")String  args) {
        @Adapt(template = "${}",ref = "Entity")
        String someLocalvar ;
    }
}