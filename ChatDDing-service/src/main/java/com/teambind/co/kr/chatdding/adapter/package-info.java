/**
 * Adapter Layer - 외부 시스템 연결
 *
 * <p>외부 시스템과의 통신을 담당하는 계층입니다.</p>
 *
 * <ul>
 *   <li>in/web - REST API Controller (Driving Adapter)</li>
 *   <li>out/persistence - MongoDB Repository (Driven Adapter)</li>
 *   <li>out/messaging - Kafka Producer (Driven Adapter)</li>
 *   <li>out/cache - Redis Cache (Driven Adapter)</li>
 * </ul>
 */
package com.teambind.co.kr.chatdding.adapter;
