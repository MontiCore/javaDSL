package Concrete;

class ServiceSupport {
  private SupportValue value = new SupportValue();

  Service decorate(Service service) {
    return value.apply(service);
  }
}
