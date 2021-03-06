package com.octoperf.metrics.windows.pdh;

import com.octoperf.metrics.service.api.GaugeService;
import com.octoperf.metrics.windows.pdh.api.PerfmonQueryService;
import com.octoperf.metrics.windows.pdh.api.WindowsNetworkInterfaceMetrics;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.win32.Pdh;
import org.hyperic.sigar.win32.Win32Exception;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.stereotype.Component;

import javax.management.JMException;
import javax.management.ObjectName;
import java.util.List;

import static java.lang.String.format;
import static javax.management.ObjectName.quote;

@Slf4j
@Component
@ConditionalOnBean(Pdh.class)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class PdhNetworkInterfaces {
  private static final String IFACE = "Network Interface";
  private static final String OBJECT_NAME = "Windows:type=NetworkInterface,instance=%s";

  PdhNetworkInterfaces(
    final PerfmonQueryService perfmon,
    final GaugeService gauges,
    final AnnotationMBeanExporter exporter) throws Win32Exception, JMException {
    super();

    final List<String> instances = perfmon.getInstances(IFACE);
    log.info("Network Interfaces: " + instances);
    for(final String instance: instances) {
      final ObjectName objectName = new ObjectName(format(OBJECT_NAME, quote(instance)));
      final WindowsNetworkInterfaceMetrics metric = new PdhNetworkInterfaceMetrics(perfmon, gauges, instance);
      exporter.registerManagedResource(metric, objectName);
    }
  }
}
