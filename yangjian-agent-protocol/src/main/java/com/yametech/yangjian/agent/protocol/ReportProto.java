// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: report.proto

package com.yametech.yangjian.agent.protocol;

public final class ReportProto {
  private ReportProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ReportResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReportResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ReportRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReportRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ReportEntity_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReportEntity_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ReportEntity_ParamsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReportEntity_ParamsEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014report.proto\"\035\n\016ReportResponse\022\013\n\003msg\030" +
      "\001 \001(\t\"4\n\rReportRequest\022#\n\014reportEntity\030\001" +
      " \003(\0132\r.ReportEntity\"\242\001\n\014ReportEntity\022\023\n\013" +
      "serviceName\030\001 \001(\t\022\020\n\010instance\030\002 \001(\t\022\021\n\tt" +
      "imestamp\030\003 \001(\003\022)\n\006params\030\004 \003(\0132\031.ReportE" +
      "ntity.ParamsEntry\032-\n\013ParamsEntry\022\013\n\003key\030" +
      "\001 \001(\t\022\r\n\005value\030\002 \001(\t:\0028\0012<\n\rReportServic" +
      "e\022+\n\006report\022\016.ReportRequest\032\017.ReportResp" +
      "onse\"\000B5\n$com.yametech.yangjian.agent.pr" +
      "otocolB\013ReportProtoP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_ReportResponse_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_ReportResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ReportResponse_descriptor,
        new java.lang.String[] { "Msg", });
    internal_static_ReportRequest_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_ReportRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ReportRequest_descriptor,
        new java.lang.String[] { "ReportEntity", });
    internal_static_ReportEntity_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_ReportEntity_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ReportEntity_descriptor,
        new java.lang.String[] { "ServiceName", "Instance", "Timestamp", "Params", });
    internal_static_ReportEntity_ParamsEntry_descriptor =
      internal_static_ReportEntity_descriptor.getNestedTypes().get(0);
    internal_static_ReportEntity_ParamsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ReportEntity_ParamsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}