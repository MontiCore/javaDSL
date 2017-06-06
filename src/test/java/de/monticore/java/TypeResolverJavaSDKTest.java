/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.monticore.java;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.monticore.java.types.JavaDSLTypeChecker;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 22.10.2016.
 */
public class TypeResolverJavaSDKTest extends AbstractCoCoTestClass {

  JavaDSLTypeChecker checker = new JavaDSLTypeChecker();

  @BeforeClass
  public static void init() {
    Log.enableFailQuick(false);
  }

  @Before
  public void setUp() {
    Log.getFindings().clear();
  }

  /*
   * java.lang.annotation
   */
  @Test
  public void TestAnnotation() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/Annotation", checker.getAllTypeChecker());
  }

  @Test
  public void TestAnnotationFormatError() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/AnnotationFormatError", checker.getAllTypeChecker());
  }


  @Test
  public void TestAnnotationTypeMismatchException() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/AnnotationTypeMismatchException", checker.getAllTypeChecker());
  }


  @Test
  public void TestDocumented() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/Documented", checker.getAllTypeChecker());
  }

  @Test
  public void TestElementType() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/ElementType", checker.getAllTypeChecker());
  }

  @Test
  public void TestIncompleteAnnotationException() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/IncompleteAnnotationException", checker.getAllTypeChecker());
  }

  @Test
  public void TestInherited() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/Inherited", checker.getAllTypeChecker());
  }

  @Test
  public void TestRetention() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/Retention", checker.getAllTypeChecker());
  }

  @Test
  public void TestRetentionPolicy() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/RetentionPolicy", checker.getAllTypeChecker());
  }

  @Test
  public void TestTarget() {
    testModelNoErrors("src/test/resources",
        "java/lang/annotation/Target", checker.getAllTypeChecker());
  }

/*
 java.lang.instrument package
 */

  @Test
  public void TestClassDefinition() {
    testModelNoErrors("src/test/resources",
        "java/lang/instrument/ClassDefinition", checker.getAllTypeChecker());
  }

  @Test
  public void TestClassFileTransformer() {
    testModelNoErrors("src/test/resources",
        "java/lang/instrument/ClassFileTransformer", checker.getAllTypeChecker());
  }

  @Test
  public void TestIllegalClassFormatException() {
    testModelNoErrors("src/test/resources",
        "java/lang/instrument/IllegalClassFormatException", checker.getAllTypeChecker());
  }

  @Test
  public void TestInstrumentation() {
    testModelNoErrors("src/test/resources",
        "java/lang/instrument/Instrumentation", checker.getAllTypeChecker());
  }

  @Test
  public void TestUnmodifiableClassException() {
    testModelNoErrors("src/test/resources",
        "java/lang/instrument/UnmodifiableClassException", checker.getAllTypeChecker());
  }

  /*
   * java.lang.management package
   */

  @Test
  public void TestClassLoadingMXBean() {
    testModelNoErrors("src/test/resources",
        "java/lang/management/ClassLoadingMXBean", checker.getAllTypeChecker());
  }


  @Test
  public void TestCompilationMXBean() {
    testModelNoErrors("src/test/resources",
        "java/lang/management/CompilationMXBean", checker.getAllTypeChecker());
  }

  @Test
  public void TestMemoryManagerMXBean() {
    testModelNoErrors("src/test/resources",
        "java/lang/management/MemoryManagerMXBean", checker.getAllTypeChecker());
  }

  
  @Test
  public void TestLockInfo() {
    testModelNoErrors("src/test/resources",
        "java/lang/management/LockInfo", checker.getAllTypeChecker());
  }


  @Test
  public void TestGarbageCollectorMXBean() {
    testModelNoErrors("src/test/resources",
        "java/lang/management/GarbageCollectorMXBean", checker.getAllTypeChecker());
  }
  //  /*
//  Reflect
//   */
//
  @Test
  public void TestAnnotatedElement() {
    testModelNoErrors("src/test/resources",
        "java/lang/reflect/AnnotatedElement", checker.getAllTypeChecker());
  }

  @Test
  public void TestGenericDeclaration() {
    testModelNoErrors("src/test/resources",
        "java/lang/reflect/GenericDeclaration", checker.getAllTypeChecker());
  }


  @Test
  public void TestReflectPermission() {
    testModelNoErrors("src/test/resources",
        "java/lang/reflect/ReflectPermission", checker.getAllTypeChecker());
  }

  @Test
  public void TestType() {
    testModelNoErrors("src/test/resources",
        "java/lang/reflect/Type", checker.getAllTypeChecker());
  }

  @Test
  public void TestTypeVariable() {
    testModelNoErrors("src/test/resources",
        "java/lang/reflect/TypeVariable", checker.getAllTypeChecker());
  }

  @Test
  public void TestUndeclaredThrowableException() {
    testModelNoErrors("src/test/resources",
        "java/lang/reflect/UndeclaredThrowableException", checker.getAllTypeChecker());
  }

  @Test
  public void TestWildcardType() {
    testModelNoErrors("src/test/resources",
        "java/lang/reflect/WildcardType", checker.getAllTypeChecker());
  }



  /*
  Normal
   */

  @Test
  public void TestRuntimeException() {
    testModelNoErrors("src/test/resources",
        "java/lang/RuntimeException", checker.getAllTypeChecker());
  }
//
//  @Test
//  public void TestClass() {
//        Collection<Finding> expectedErrors = Arrays.asList(
//            Finding.error(
//                "0xA0550 the relational expression operand of the 'instanceof' operator must be a reference type or the null type."),
//            Finding.error(
//                "0xA0551 the reference type mentioned after the 'instanceof' operator must denote a reference type."),
//            Finding.error(
//                "0xA0552 the reference type mentioned after the 'instanceof' operator must denote a reifiable type."),
//            Finding.error("0xA0553 incompatible conditional operand types 'Integer', 'String'.")
//        );
//    testModelForErrors("src/test/resources",
//        "java/lang/Class", checker.getAllTypeChecker(),expectedErrors);
//  }

//
//  @Test
//  public void TestMath() {
//    Collection<Finding> expectedErrors = Arrays.asList(
//        Finding.error(
//            "0xA0550 the relational expression operand of the 'instanceof' operator must be a reference type or the null type."),
//        Finding.error(
//            "0xA0551 the reference type mentioned after the 'instanceof' operator must denote a reference type."),
//        Finding.error(
//            "0xA0552 the reference type mentioned after the 'instanceof' operator must denote a reifiable type."),
//        Finding.error("0xA0553 incompatible conditional operand types 'Integer', 'String'.")
//    );
//    //// TODO: 01.11.2016 2 doubles
//    testModelForErrors("src/test/resources",
//        "java/lang/Math", checker.getAllTypeChecker(),expectedErrors);
//  }



  @Test
  public void TestIncompatibleClassChangeError() {
    testModelNoErrors("src/test/resources",
        "java/lang/IncompatibleClassChangeError", checker.getAllTypeChecker());
  }

  @Test
  public void TestLinkageError() {
    testModelNoErrors("src/test/resources",
        "java/lang/LinkageError", checker.getAllTypeChecker());
  }

  @Test
  public void TestAbstractMethodError() {
    testModelNoErrors("src/test/resources",
        "java/lang/AbstractMethodError", checker.getAllTypeChecker());
  }

  @Test
  public void TestAppendable() {
    testModelNoErrors("src/test/resources",
        "java/lang/Appendable", checker.getAllTypeChecker());
  }


  @Test
  public void TestFlushable() {
    testModelNoErrors("src/test/resources",
        "java/io/Flushable", checker.getAllTypeChecker());
  }

  @Test
  public void TestOutputStream() {
    testModelNoErrors("src/test/resources",
        "java/io/OutputStream", checker.getAllTypeChecker());
  }

  @Test
  public void TestDataOutput() {
    testModelNoErrors("src/test/resources",
        "java/io/DataOutput", checker.getAllTypeChecker());
  }

  @Test
  public void TestObjectOutput() {
    testModelNoErrors("src/test/resources",
        "java/io/ObjectOutput", checker.getAllTypeChecker());
  }

  @Test
  public void TestStringBuilder() {
    testModelNoErrors("src/test/resources",
        "java/lang/StringBuilder", checker.getAllTypeChecker());
  }
//
//  @Test
//  public void TestStringBuffer() {
//    testModelNoErrors("src/test/resources",
//        "java/lang/StringBuffer", checker.getAllTypeChecker());
//  }
//
//  @Test
//  public void TestStrictMath() {
//            Collection<Finding> expectedErrors = Arrays.asList(
//                Finding.error(
//                    "0xA0550 the relational expression operand of the 'instanceof' operator must be a reference type or the null type."),
//                Finding.error(
//                    "0xA0551 the reference type mentioned after the 'instanceof' operator must denote a reference type."),
//                Finding.error(
//                    "0xA0552 the reference type mentioned after the 'instanceof' operator must denote a reifiable type."),
//                Finding.error("0xA0553 incompatible conditional operand types 'Integer', 'String'.")
//            );
//    testModelForErrors("src/test/resources",
//        "java/lang/StrictMath", checker.getAllTypeChecker(),expectedErrors);
//  }

  @Test
  public void TestByte() {
    testModelNoErrors("src/test/resources",
        "java/lang/Byte", checker.getAllTypeChecker());
  }

  @Test
  public void TestShort() {
    testModelNoErrors("src/test/resources",
        "java/lang/Short", checker.getAllTypeChecker());
  }

  @Test
  public void TestLong() {
    testModelNoErrors("src/test/resources",
        "java/lang/Long", checker.getAllTypeChecker());
  }

  @Test
  public void TestBoolean() {
    testModelNoErrors("src/test/resources",
        "java/lang/Boolean", checker.getAllTypeChecker());
  }


  @Test
  public void TestMember() {
    testModelNoErrors("src/test/resources",
        "java/lang/reflect/Member", checker.getAllTypeChecker());
  }

  @Test
  public void TestObjectStreamClass() {
    testModelNoErrors("src/test/resources",
        "java/io/ObjectStreamClass", checker.getAllTypeChecker());
  }

  @Test
  public void TestObjectStreamField() {
    testModelNoErrors("src/test/resources",
        "java/io/ObjectStreamField", checker.getAllTypeChecker());
  }

  @Test
  public void TestObjectStreamException() {
    testModelNoErrors("src/test/resources",
        "java/io/ObjectStreamException", checker.getAllTypeChecker());
  }

  @Test
  public void TestStreamCorruptedException() {
    testModelNoErrors("src/test/resources",
        "java/io/StreamCorruptedException", checker.getAllTypeChecker());
  }

  @Test
  public void TestClassNotFoundException() {
    testModelNoErrors("src/test/resources",
        "java/lang/ClassNotFoundException", checker.getAllTypeChecker());
  }


  @Test
  public void TestIOException() {
    testModelNoErrors("src/test/resources",
        "java/io/IOException", checker.getAllTypeChecker());
  }

  @Test
  public void TestDataInput() {
    testModelNoErrors("src/test/resources",
        "java/io/DataInput", checker.getAllTypeChecker());
  }

  @Test
  public void TestObjectInput() {
    testModelNoErrors("src/test/resources",
        "java/io/ObjectInput", checker.getAllTypeChecker());
  }



  @Test
  public void TestObjectStreamConstants() {
    testModelNoErrors("src/test/resources",
        "java/io/ObjectStreamConstants", checker.getAllTypeChecker());
  }


  @Test
  @Ignore
  public void TestRandom() {
    testModelNoErrors("src/test/resources",
        "java/util/Random", checker.getAllTypeChecker());
  }


  @Test
  public void TestAtomicLong() {
    testModelNoErrors("src/test/resources",
        "java/util/concurrent/atomic/AtomicLong", checker.getAllTypeChecker());
  }


  @Test
  public void TestNullPointerException() {
    testModelNoErrors("src/test/resources",
        "java/lang/NullPointerException", checker.getAllTypeChecker());
  }


  @Test
  public void TestSecurityException() {
    testModelNoErrors("src/test/resources",
        "java/lang/SecurityException", checker.getAllTypeChecker());
  }
  @Test
  public void TestError() {
    testModelNoErrors("src/test/resources",
        "java/lang/Error", checker.getAllTypeChecker());
  }
//
//  @Test
//  public void TestFloat() {
//    //// TODO: 26.10.2016 finish math, misc classes first
//    Collection<Finding> expectedErrors = Arrays.asList(
//        Finding.error(
//            "0xA0550 the relational expression operand of the 'instanceof' operator must be a reference type or the null type."),
//        Finding.error(
//            "0xA0551 the reference type mentioned after the 'instanceof' operator must denote a reference type."),
//        Finding.error(
//            "0xA0552 the reference type mentioned after the 'instanceof' operator must denote a reifiable type."),
//        Finding.error("0xA0553 incompatible conditional operand types 'Integer', 'String'.")
//    );
//    testModelForErrors("src/test/resources",
//        "java/lang/Float", checker.getAllTypeChecker(),expectedErrors);
//  }

  //
  @Test
  public void TestDouble() {
    testModelNoErrors("src/test/resources",
        "java/lang/Double", checker.getAllTypeChecker());
  }

  @Test
  public void TestNumberFormatException() {
    testModelNoErrors("src/test/resources",
        "java/lang/NumberFormatException", checker.getAllTypeChecker());
  }


  @Test
  public void TestInteger() {
    testModelNoErrors("src/test/resources",
        "java/lang/Integer", checker.getAllTypeChecker());
  }

  @Test
  public void TestNumber() {
    testModelNoErrors("src/test/resources",
        "java/lang/Number", checker.getAllTypeChecker());
  }


  @Test
  public void TestStringValue() {
    testModelNoErrors("src/test/resources",
        "java/lang/StringValue", checker.getAllTypeChecker());
  }
  @Test
  public void TestString() {
    testModelNoErrors("src/test/resources",
        "java/lang/String", checker.getAllTypeChecker());
  }

  @Test
  public void TestComparable() {
    testModelNoErrors("src/test/resources",
        "java/lang/Comparable", checker.getAllTypeChecker());
  }

  @Test
  public void TestCharacter() {
    testModelNoErrors("src/test/resources",
        "java/lang/Character", checker.getAllTypeChecker());
  }

  @Test
  public void TestCharacterData00() {
    testModelNoErrors("src/test/resources",
        "java/lang/CharacterData00", checker.getAllTypeChecker());
  }

  @Test
  public void TestCharacterData0E() {
    testModelNoErrors("src/test/resources",
        "java/lang/CharacterData0E", checker.getAllTypeChecker());
  }

  @Test
  public void TestCharacterData01() {
    testModelNoErrors("src/test/resources",
        "java/lang/CharacterData01", checker.getAllTypeChecker());
  }

  @Test
  public void TestCharacterData02() {
    testModelNoErrors("src/test/resources",
        "java/lang/CharacterData02", checker.getAllTypeChecker());
  }


  @Test
  public void TestCharacterDataLatin1() {
    testModelNoErrors("src/test/resources",
        "java/lang/CharacterDataLatin1", checker.getAllTypeChecker());
  }


  @Test
  public void TestCharacterDataPrivateUse() {
    testModelNoErrors("src/test/resources",
        "java/lang/CharacterDataPrivateUse", checker.getAllTypeChecker());
  }

  @Test
  public void TestCharacterDataUndefined() {
    testModelNoErrors("src/test/resources",
        "java/lang/CharacterDataUndefined", checker.getAllTypeChecker());
  }

  @Test
  public void TestCharSequence() {
    testModelNoErrors("src/test/resources",
        "java/lang/CharSequence", checker.getAllTypeChecker());
  }

  @Test
  public void TestIllegalArgumentException() {
    testModelNoErrors("src/test/resources",
        "java/lang/IllegalArgumentException", checker.getAllTypeChecker());
  }
  @Test
  public void TestObject() {
    testModelNoErrors("src/test/resources",
        "java/lang/Object", checker.getAllTypeChecker());
  }

  @Test
  public void TestInterruptedException() {
    testModelNoErrors("src/test/resources",
        "java/lang/InterruptedException", checker.getAllTypeChecker());
  }

  @Test
  public void TestStringIndexOutOfBoundsException() {
    testModelNoErrors("src/test/resources",
        "java/lang/StringIndexOutOfBoundsException", checker.getAllTypeChecker());
  }

  @Test
  public void TestCloneNotSupportedException() {
    testModelNoErrors("src/test/resources",
        "java/lang/CloneNotSupportedException", checker.getAllTypeChecker());
  }

  @Test
  public void TestCloseable() {
    testModelNoErrors("src/test/resources",
        "java/io/Closeable", checker.getAllTypeChecker());
  }

  @Test
  public void TestSerializable() {
    testModelNoErrors("src/test/resources",
        "java/io/Serializable", checker.getAllTypeChecker());
  }
//  /*
//  sun.misc
//   */
//
  @Test
  public void TestFloatConsts() {
    testModelNoErrors("src/test/resources",
        "sun/misc/FloatConsts", checker.getAllTypeChecker());
  }


  @Test
  public void TestDoubleConsts() {
    testModelNoErrors("src/test/resources",
        "sun/misc/DoubleConsts", checker.getAllTypeChecker());
  }

  @Test
  @Ignore
  public void TestFpUtils() {
    testModelNoErrors("src/test/resources",
        "sun/misc/FpUtils", checker.getAllTypeChecker());
  }

//  /*
//  sun.util
//   */
//
//  @Test
//  public void TestLocaleDataMetaInfo() {
//    Collection<Finding> expectedErrors = Arrays.asList(
//        Finding.error("0xA0506 an array component type must be a reifiable type."),
//        Finding.error("0xA0506 an array component type must be a reifiable type.")
//    );
//    testModelForErrors("src/test/resources",
//        "sunn/util/LocaleDataMetaInfo", checker.getAllTypeChecker(),expectedErrors);
//  }
//  /*
//  security
//   */
//

  @Test
  public void TestGuard() {
    testModelNoErrors("src/test/resources",
        "java/security/Guard", checker.getAllTypeChecker());
  }
//
//  @Test
//  public void TestPermission() {
//    Collection<Finding> expectedErrors = Arrays.asList(
//        Finding.error("0xA0506 an array component type must be a reifiable type."),
//        Finding.error("0xA0506 an array component type must be a reifiable type.")
//    );
//    testModelForErrors("src/test/resources",
//        "java/security/Permission", checker.getAllTypeChecker(),expectedErrors);
//  }
//
  /*
  java.util
   */
  @Test
  public void TestMap() {
    testModelNoErrors("src/test/resources",
        "java/util/Map", checker.getAllTypeChecker());
  }

  @Test
  public void TestSet() {
    testModelNoErrors("src/test/resources",
        "java/util/Set", checker.getAllTypeChecker());
  }

  @Test
  public void TestUnsupportedOperationException() {
    testModelNoErrors("src/test/resources",
        "java/lang/UnsupportedOperationException", checker.getAllTypeChecker());
  }

  @Test
  public void TestNegativeArraySizeException() {
    testModelNoErrors("src/test/resources",
        "java/lang/NegativeArraySizeException", checker.getAllTypeChecker());
  }
//
//  @Test
//  public void TestArray() {
//    //// TODO: 24.10.2016 finish system and math first
//    testModelNoErrors("src/test/resources",
//        "java/lang/reflect/Array", checker.getAllTypeChecker());
//  }
//
//  @Test
//  public void TestAbstractCollection() {
//    //// TODO: 24.10.2016 finish Array first
//    Collection<Finding> expectedErrors = Arrays.asList(
//        Finding.error("0xA0506 an array component type must be a reifiable type."),
//        Finding.error("0xA0506 an array component type must be a reifiable type.")
//    );
//
//    testModelForErrors("src/test/resources",
//        "java/util/AbstractCollection", checker.getAllTypeChecker(),expectedErrors);
//  }
//

  @Test
  public void TestIterable() {
    testModelNoErrors("src/test/resources",
        "java/lang/Iterable", checker.getAllTypeChecker());
  }


  @Test
  public void TestCollection() {
    testModelNoErrors("src/test/resources",
        "java/util/Collection", checker.getAllTypeChecker());
  }
//
//  @Test
//  public void TestAbstractSet() {
//    testModelNoErrors("src/test/resources",
//        "java/util/AbstractSet", checker.getAllTypeChecker());
//  }

//  @Test
//  public void TestAbstractMap() {
//    ASTCompilationUnit ast = parse("src/test/resources",
//        "java/util/AbstractMap");
//    Collection<Finding> expectedErrors = Arrays.asList(
//        Finding.error("0xA0506 an array component type must be a reifiable type."),
//        Finding.error("0xA0506 an array component type must be a reifiable type.")
//    );
//    testModelForErrors("src/test/resources",
//        "java/util/AbstractMap", checker.getAllTypeChecker(),expectedErrors);
//  }



  @Test
  public void TestIterator() {
    testModelNoErrors("src/test/resources",
        "java/util/Iterator", checker.getAllTypeChecker());
  }

  @Test
  public void TestConcurrentMap() {
    testModelNoErrors("src/test/resources",
        "java/util/concurrent/ConcurrentMap", checker.getAllTypeChecker());
  }

  @Test
  public void TestCharConversionException() {
    testModelNoErrors("src/test/resources",
        "java/io/CharConversionException", checker.getAllTypeChecker());
  }

//  @Test
//  public void TestConcurrentHashMap() {
//    Collection<Finding> expectedErrors = Arrays.asList(
//        Finding.error("0xA0506 an array component type must be a reifiable type."),
//        Finding.error("0xA0506 an array component type must be a reifiable type.")
//    );
//    testModelForErrors("src/test/resources",
//        "java/util/concurrent/ConcurrentHashMap", checker.getAllTypeChecker(),expectedErrors);
//  }

//  @Test
//  public void TestLocale() {
//        Collection<Finding> expectedErrors = Arrays.asList(
//            Finding.error("0xA0506 an array component type must be a reifiable type."),
//            Finding.error("0xA0506 an array component type must be a reifiable type.")
//        );
//    testModelForErrors("src/test/resources",
//        "java/util/Locale", checker.getAllTypeChecker(),expectedErrors);
//  }


  @Test
  public void TestLocaleServiceProvider() {
    testModelNoErrors("src/test/resources",
        "java/util/spi/LocaleServiceProvider", checker.getAllTypeChecker());
  }

  @Test
  public void TestCurrencyNameProvider() {
    testModelNoErrors("src/test/resources",
        "java/util/spi/CurrencyNameProvider", checker.getAllTypeChecker());
  }

  @Test
  public void TestLocaleNameProvider() {
    testModelNoErrors("src/test/resources",
        "java/util/spi/LocaleNameProvider", checker.getAllTypeChecker());
  }


  @Test
  public void TestTimeZoneNameProvider() {
    testModelNoErrors("src/test/resources",
        "java/util/spi/TimeZoneNameProvider", checker.getAllTypeChecker());
  }

}
