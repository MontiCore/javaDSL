import com.example.boing.Blib;
import com.example.boing.zlong.*;

import static com.example.fluff.*;

module com.example.foo {

  requires com.example;
  requires static com.example.something;
  requires transitive com.example.somethingelse;

  exports com.example.foo;
  exports com.example.foo.internal to com.example.friend;
  exports com.example.foo.notasinternal to com.example.friend, com.example.otherfriend;

  opens com.example.somethingopen;
  opens com.example.somethingopen.internal to com.example.friend;
  opens com.example.somethingopen.notasinternal to com.example.friend, com.example.otherfriend;

  uses SomeService;
  uses com.example.SomeOtherService;

  provides SomeService with SomeServiceImplementation;
  provides com.example.SomeOtherService with SomeOtherServiceImplementation, com.example.AnotherOtherServiceImplementation;

}