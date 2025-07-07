package com.imd.benchmark.jmh_generated;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collection;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.runner.InfraControl;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.results.BenchmarkTaskResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ThroughputResult;
import org.openjdk.jmh.results.AverageTimeResult;
import org.openjdk.jmh.results.SampleTimeResult;
import org.openjdk.jmh.results.SingleShotResult;
import org.openjdk.jmh.util.SampleBuffer;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.results.RawResults;
import org.openjdk.jmh.results.ResultRole;
import java.lang.reflect.Field;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.Control;
import org.openjdk.jmh.results.ScalarResult;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.runner.FailureAssistException;

import com.imd.benchmark.jmh_generated.IDWBenchmark_EstadoDoBenchmark_jmhType;
import com.imd.benchmark.jmh_generated.IDWBenchmark_jmhType;
public final class IDWBenchmark_benchmarkProcessarLote_jmhTest {

    byte p000, p001, p002, p003, p004, p005, p006, p007, p008, p009, p010, p011, p012, p013, p014, p015;
    byte p016, p017, p018, p019, p020, p021, p022, p023, p024, p025, p026, p027, p028, p029, p030, p031;
    byte p032, p033, p034, p035, p036, p037, p038, p039, p040, p041, p042, p043, p044, p045, p046, p047;
    byte p048, p049, p050, p051, p052, p053, p054, p055, p056, p057, p058, p059, p060, p061, p062, p063;
    byte p064, p065, p066, p067, p068, p069, p070, p071, p072, p073, p074, p075, p076, p077, p078, p079;
    byte p080, p081, p082, p083, p084, p085, p086, p087, p088, p089, p090, p091, p092, p093, p094, p095;
    byte p096, p097, p098, p099, p100, p101, p102, p103, p104, p105, p106, p107, p108, p109, p110, p111;
    byte p112, p113, p114, p115, p116, p117, p118, p119, p120, p121, p122, p123, p124, p125, p126, p127;
    byte p128, p129, p130, p131, p132, p133, p134, p135, p136, p137, p138, p139, p140, p141, p142, p143;
    byte p144, p145, p146, p147, p148, p149, p150, p151, p152, p153, p154, p155, p156, p157, p158, p159;
    byte p160, p161, p162, p163, p164, p165, p166, p167, p168, p169, p170, p171, p172, p173, p174, p175;
    byte p176, p177, p178, p179, p180, p181, p182, p183, p184, p185, p186, p187, p188, p189, p190, p191;
    byte p192, p193, p194, p195, p196, p197, p198, p199, p200, p201, p202, p203, p204, p205, p206, p207;
    byte p208, p209, p210, p211, p212, p213, p214, p215, p216, p217, p218, p219, p220, p221, p222, p223;
    byte p224, p225, p226, p227, p228, p229, p230, p231, p232, p233, p234, p235, p236, p237, p238, p239;
    byte p240, p241, p242, p243, p244, p245, p246, p247, p248, p249, p250, p251, p252, p253, p254, p255;
    int startRndMask;
    BenchmarkParams benchmarkParams;
    IterationParams iterationParams;
    ThreadParams threadParams;
    Blackhole blackhole;
    Control notifyControl;

    public BenchmarkTaskResult benchmarkProcessarLote_Throughput(InfraControl control, ThreadParams threadParams) throws Throwable {
        this.benchmarkParams = control.benchmarkParams;
        this.iterationParams = control.iterationParams;
        this.threadParams    = threadParams;
        this.notifyControl   = control.notifyControl;
        if (this.blackhole == null) {
            this.blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        }
        if (threadParams.getSubgroupIndex() == 0) {
            RawResults res = new RawResults();
            IDWBenchmark_jmhType l_idwbenchmark0_0 = _jmh_tryInit_f_idwbenchmark0_0(control);
            IDWBenchmark_EstadoDoBenchmark_jmhType l_estadodobenchmark1_G = _jmh_tryInit_f_estadodobenchmark1_G(control);

            control.preSetup();


            control.announceWarmupReady();
            while (control.warmupShouldWait) {
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (!l_estadodobenchmark1_G.readyInvocation) {
                            l_estadodobenchmark1_G.setupInvocation();
                            l_estadodobenchmark1_G.readyInvocation = true;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (l_estadodobenchmark1_G.readyInvocation) {
                            l_estadodobenchmark1_G.readyInvocation = false;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                if (control.shouldYield) Thread.yield();
                res.allOps++;
            }

            notifyControl.startMeasurement = true;
            benchmarkProcessarLote_thrpt_jmhStub(control, res, benchmarkParams, iterationParams, threadParams, blackhole, notifyControl, startRndMask, l_estadodobenchmark1_G, l_idwbenchmark0_0);
            notifyControl.stopMeasurement = true;
            control.announceWarmdownReady();
            try {
                while (control.warmdownShouldWait) {
                    if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                        try {
                            if (control.isFailing) throw new FailureAssistException();
                            if (!l_estadodobenchmark1_G.readyInvocation) {
                                l_estadodobenchmark1_G.setupInvocation();
                                l_estadodobenchmark1_G.readyInvocation = true;
                            }
                        } catch (Throwable t) {
                            control.isFailing = true;
                            throw t;
                        } finally {
                            IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                        }
                    } else {
                        while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                            if (control.isFailing) throw new FailureAssistException();
                            if (Thread.interrupted()) throw new InterruptedException();
                        }
                    }
                    l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
                    if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                        try {
                            if (control.isFailing) throw new FailureAssistException();
                            if (l_estadodobenchmark1_G.readyInvocation) {
                                l_estadodobenchmark1_G.readyInvocation = false;
                            }
                        } catch (Throwable t) {
                            control.isFailing = true;
                            throw t;
                        } finally {
                            IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                        }
                    } else {
                        while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                            if (control.isFailing) throw new FailureAssistException();
                            if (Thread.interrupted()) throw new InterruptedException();
                        }
                    }
                    if (control.shouldYield) Thread.yield();
                    res.allOps++;
                }
            } catch (Throwable e) {
                if (!(e instanceof InterruptedException)) throw e;
            }
            control.preTearDown();

            if (control.isLastIteration()) {
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (l_estadodobenchmark1_G.readyTrial) {
                            l_estadodobenchmark1_G.tearDownTrial();
                            l_estadodobenchmark1_G.readyTrial = false;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    long l_estadodobenchmark1_G_backoff = 1;
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        TimeUnit.MILLISECONDS.sleep(l_estadodobenchmark1_G_backoff);
                        l_estadodobenchmark1_G_backoff = Math.max(1024, l_estadodobenchmark1_G_backoff * 2);
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                synchronized(this.getClass()) {
                    f_estadodobenchmark1_G = null;
                }
                f_idwbenchmark0_0 = null;
            }
            res.allOps += res.measuredOps;
            int batchSize = iterationParams.getBatchSize();
            int opsPerInv = benchmarkParams.getOpsPerInvocation();
            res.allOps *= opsPerInv;
            res.allOps /= batchSize;
            res.measuredOps *= opsPerInv;
            res.measuredOps /= batchSize;
            BenchmarkTaskResult results = new BenchmarkTaskResult((long)res.allOps, (long)res.measuredOps);
            results.add(new ThroughputResult(ResultRole.PRIMARY, "benchmarkProcessarLote", res.measuredOps, res.getTime(), benchmarkParams.getTimeUnit()));
            this.blackhole.evaporate("Yes, I am Stephen Hawking, and know a thing or two about black holes.");
            return results;
        } else
            throw new IllegalStateException("Harness failed to distribute threads among groups properly");
    }

    public static void benchmarkProcessarLote_thrpt_jmhStub(InfraControl control, RawResults result, BenchmarkParams benchmarkParams, IterationParams iterationParams, ThreadParams threadParams, Blackhole blackhole, Control notifyControl, int startRndMask, IDWBenchmark_EstadoDoBenchmark_jmhType l_estadodobenchmark1_G, IDWBenchmark_jmhType l_idwbenchmark0_0) throws Throwable {
        long operations = 0;
        long realTime = 0;
        result.startTime = System.nanoTime();
        do {
            if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                try {
                    if (control.isFailing) throw new FailureAssistException();
                    if (!l_estadodobenchmark1_G.readyInvocation) {
                        l_estadodobenchmark1_G.setupInvocation();
                        l_estadodobenchmark1_G.readyInvocation = true;
                    }
                } catch (Throwable t) {
                    control.isFailing = true;
                    throw t;
                } finally {
                    IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                }
            } else {
                while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                    if (control.isFailing) throw new FailureAssistException();
                    if (Thread.interrupted()) throw new InterruptedException();
                }
            }
            long rt = System.nanoTime();
            l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
            realTime += (System.nanoTime() - rt);
            if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                try {
                    if (control.isFailing) throw new FailureAssistException();
                    if (l_estadodobenchmark1_G.readyInvocation) {
                        l_estadodobenchmark1_G.readyInvocation = false;
                    }
                } catch (Throwable t) {
                    control.isFailing = true;
                    throw t;
                } finally {
                    IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                }
            } else {
                while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                    if (control.isFailing) throw new FailureAssistException();
                    if (Thread.interrupted()) throw new InterruptedException();
                }
            }
            operations++;
        } while(!control.isDone);
        result.stopTime = System.nanoTime();
        result.realTime = realTime;
        result.measuredOps = operations;
    }


    public BenchmarkTaskResult benchmarkProcessarLote_AverageTime(InfraControl control, ThreadParams threadParams) throws Throwable {
        this.benchmarkParams = control.benchmarkParams;
        this.iterationParams = control.iterationParams;
        this.threadParams    = threadParams;
        this.notifyControl   = control.notifyControl;
        if (this.blackhole == null) {
            this.blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        }
        if (threadParams.getSubgroupIndex() == 0) {
            RawResults res = new RawResults();
            IDWBenchmark_jmhType l_idwbenchmark0_0 = _jmh_tryInit_f_idwbenchmark0_0(control);
            IDWBenchmark_EstadoDoBenchmark_jmhType l_estadodobenchmark1_G = _jmh_tryInit_f_estadodobenchmark1_G(control);

            control.preSetup();


            control.announceWarmupReady();
            while (control.warmupShouldWait) {
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (!l_estadodobenchmark1_G.readyInvocation) {
                            l_estadodobenchmark1_G.setupInvocation();
                            l_estadodobenchmark1_G.readyInvocation = true;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (l_estadodobenchmark1_G.readyInvocation) {
                            l_estadodobenchmark1_G.readyInvocation = false;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                if (control.shouldYield) Thread.yield();
                res.allOps++;
            }

            notifyControl.startMeasurement = true;
            benchmarkProcessarLote_avgt_jmhStub(control, res, benchmarkParams, iterationParams, threadParams, blackhole, notifyControl, startRndMask, l_estadodobenchmark1_G, l_idwbenchmark0_0);
            notifyControl.stopMeasurement = true;
            control.announceWarmdownReady();
            try {
                while (control.warmdownShouldWait) {
                    if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                        try {
                            if (control.isFailing) throw new FailureAssistException();
                            if (!l_estadodobenchmark1_G.readyInvocation) {
                                l_estadodobenchmark1_G.setupInvocation();
                                l_estadodobenchmark1_G.readyInvocation = true;
                            }
                        } catch (Throwable t) {
                            control.isFailing = true;
                            throw t;
                        } finally {
                            IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                        }
                    } else {
                        while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                            if (control.isFailing) throw new FailureAssistException();
                            if (Thread.interrupted()) throw new InterruptedException();
                        }
                    }
                    l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
                    if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                        try {
                            if (control.isFailing) throw new FailureAssistException();
                            if (l_estadodobenchmark1_G.readyInvocation) {
                                l_estadodobenchmark1_G.readyInvocation = false;
                            }
                        } catch (Throwable t) {
                            control.isFailing = true;
                            throw t;
                        } finally {
                            IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                        }
                    } else {
                        while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                            if (control.isFailing) throw new FailureAssistException();
                            if (Thread.interrupted()) throw new InterruptedException();
                        }
                    }
                    if (control.shouldYield) Thread.yield();
                    res.allOps++;
                }
            } catch (Throwable e) {
                if (!(e instanceof InterruptedException)) throw e;
            }
            control.preTearDown();

            if (control.isLastIteration()) {
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (l_estadodobenchmark1_G.readyTrial) {
                            l_estadodobenchmark1_G.tearDownTrial();
                            l_estadodobenchmark1_G.readyTrial = false;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    long l_estadodobenchmark1_G_backoff = 1;
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        TimeUnit.MILLISECONDS.sleep(l_estadodobenchmark1_G_backoff);
                        l_estadodobenchmark1_G_backoff = Math.max(1024, l_estadodobenchmark1_G_backoff * 2);
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                synchronized(this.getClass()) {
                    f_estadodobenchmark1_G = null;
                }
                f_idwbenchmark0_0 = null;
            }
            res.allOps += res.measuredOps;
            int batchSize = iterationParams.getBatchSize();
            int opsPerInv = benchmarkParams.getOpsPerInvocation();
            res.allOps *= opsPerInv;
            res.allOps /= batchSize;
            res.measuredOps *= opsPerInv;
            res.measuredOps /= batchSize;
            BenchmarkTaskResult results = new BenchmarkTaskResult((long)res.allOps, (long)res.measuredOps);
            results.add(new AverageTimeResult(ResultRole.PRIMARY, "benchmarkProcessarLote", res.measuredOps, res.getTime(), benchmarkParams.getTimeUnit()));
            this.blackhole.evaporate("Yes, I am Stephen Hawking, and know a thing or two about black holes.");
            return results;
        } else
            throw new IllegalStateException("Harness failed to distribute threads among groups properly");
    }

    public static void benchmarkProcessarLote_avgt_jmhStub(InfraControl control, RawResults result, BenchmarkParams benchmarkParams, IterationParams iterationParams, ThreadParams threadParams, Blackhole blackhole, Control notifyControl, int startRndMask, IDWBenchmark_EstadoDoBenchmark_jmhType l_estadodobenchmark1_G, IDWBenchmark_jmhType l_idwbenchmark0_0) throws Throwable {
        long operations = 0;
        long realTime = 0;
        result.startTime = System.nanoTime();
        do {
            if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                try {
                    if (control.isFailing) throw new FailureAssistException();
                    if (!l_estadodobenchmark1_G.readyInvocation) {
                        l_estadodobenchmark1_G.setupInvocation();
                        l_estadodobenchmark1_G.readyInvocation = true;
                    }
                } catch (Throwable t) {
                    control.isFailing = true;
                    throw t;
                } finally {
                    IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                }
            } else {
                while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                    if (control.isFailing) throw new FailureAssistException();
                    if (Thread.interrupted()) throw new InterruptedException();
                }
            }
            long rt = System.nanoTime();
            l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
            realTime += (System.nanoTime() - rt);
            if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                try {
                    if (control.isFailing) throw new FailureAssistException();
                    if (l_estadodobenchmark1_G.readyInvocation) {
                        l_estadodobenchmark1_G.readyInvocation = false;
                    }
                } catch (Throwable t) {
                    control.isFailing = true;
                    throw t;
                } finally {
                    IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                }
            } else {
                while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                    if (control.isFailing) throw new FailureAssistException();
                    if (Thread.interrupted()) throw new InterruptedException();
                }
            }
            operations++;
        } while(!control.isDone);
        result.stopTime = System.nanoTime();
        result.realTime = realTime;
        result.measuredOps = operations;
    }


    public BenchmarkTaskResult benchmarkProcessarLote_SampleTime(InfraControl control, ThreadParams threadParams) throws Throwable {
        this.benchmarkParams = control.benchmarkParams;
        this.iterationParams = control.iterationParams;
        this.threadParams    = threadParams;
        this.notifyControl   = control.notifyControl;
        if (this.blackhole == null) {
            this.blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        }
        if (threadParams.getSubgroupIndex() == 0) {
            RawResults res = new RawResults();
            IDWBenchmark_jmhType l_idwbenchmark0_0 = _jmh_tryInit_f_idwbenchmark0_0(control);
            IDWBenchmark_EstadoDoBenchmark_jmhType l_estadodobenchmark1_G = _jmh_tryInit_f_estadodobenchmark1_G(control);

            control.preSetup();


            control.announceWarmupReady();
            while (control.warmupShouldWait) {
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (!l_estadodobenchmark1_G.readyInvocation) {
                            l_estadodobenchmark1_G.setupInvocation();
                            l_estadodobenchmark1_G.readyInvocation = true;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (l_estadodobenchmark1_G.readyInvocation) {
                            l_estadodobenchmark1_G.readyInvocation = false;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                if (control.shouldYield) Thread.yield();
                res.allOps++;
            }

            notifyControl.startMeasurement = true;
            int targetSamples = (int) (control.getDuration(TimeUnit.MILLISECONDS) * 20); // at max, 20 timestamps per millisecond
            int batchSize = iterationParams.getBatchSize();
            int opsPerInv = benchmarkParams.getOpsPerInvocation();
            SampleBuffer buffer = new SampleBuffer();
            benchmarkProcessarLote_sample_jmhStub(control, res, benchmarkParams, iterationParams, threadParams, blackhole, notifyControl, startRndMask, buffer, targetSamples, opsPerInv, batchSize, l_estadodobenchmark1_G, l_idwbenchmark0_0);
            notifyControl.stopMeasurement = true;
            control.announceWarmdownReady();
            try {
                while (control.warmdownShouldWait) {
                    if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                        try {
                            if (control.isFailing) throw new FailureAssistException();
                            if (!l_estadodobenchmark1_G.readyInvocation) {
                                l_estadodobenchmark1_G.setupInvocation();
                                l_estadodobenchmark1_G.readyInvocation = true;
                            }
                        } catch (Throwable t) {
                            control.isFailing = true;
                            throw t;
                        } finally {
                            IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                        }
                    } else {
                        while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                            if (control.isFailing) throw new FailureAssistException();
                            if (Thread.interrupted()) throw new InterruptedException();
                        }
                    }
                    l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
                    if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                        try {
                            if (control.isFailing) throw new FailureAssistException();
                            if (l_estadodobenchmark1_G.readyInvocation) {
                                l_estadodobenchmark1_G.readyInvocation = false;
                            }
                        } catch (Throwable t) {
                            control.isFailing = true;
                            throw t;
                        } finally {
                            IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                        }
                    } else {
                        while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                            if (control.isFailing) throw new FailureAssistException();
                            if (Thread.interrupted()) throw new InterruptedException();
                        }
                    }
                    if (control.shouldYield) Thread.yield();
                    res.allOps++;
                }
            } catch (Throwable e) {
                if (!(e instanceof InterruptedException)) throw e;
            }
            control.preTearDown();

            if (control.isLastIteration()) {
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (l_estadodobenchmark1_G.readyTrial) {
                            l_estadodobenchmark1_G.tearDownTrial();
                            l_estadodobenchmark1_G.readyTrial = false;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    long l_estadodobenchmark1_G_backoff = 1;
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        TimeUnit.MILLISECONDS.sleep(l_estadodobenchmark1_G_backoff);
                        l_estadodobenchmark1_G_backoff = Math.max(1024, l_estadodobenchmark1_G_backoff * 2);
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                synchronized(this.getClass()) {
                    f_estadodobenchmark1_G = null;
                }
                f_idwbenchmark0_0 = null;
            }
            res.allOps += res.measuredOps * batchSize;
            res.allOps *= opsPerInv;
            res.allOps /= batchSize;
            res.measuredOps *= opsPerInv;
            BenchmarkTaskResult results = new BenchmarkTaskResult((long)res.allOps, (long)res.measuredOps);
            results.add(new SampleTimeResult(ResultRole.PRIMARY, "benchmarkProcessarLote", buffer, benchmarkParams.getTimeUnit()));
            this.blackhole.evaporate("Yes, I am Stephen Hawking, and know a thing or two about black holes.");
            return results;
        } else
            throw new IllegalStateException("Harness failed to distribute threads among groups properly");
    }

    public static void benchmarkProcessarLote_sample_jmhStub(InfraControl control, RawResults result, BenchmarkParams benchmarkParams, IterationParams iterationParams, ThreadParams threadParams, Blackhole blackhole, Control notifyControl, int startRndMask, SampleBuffer buffer, int targetSamples, long opsPerInv, int batchSize, IDWBenchmark_EstadoDoBenchmark_jmhType l_estadodobenchmark1_G, IDWBenchmark_jmhType l_idwbenchmark0_0) throws Throwable {
        long realTime = 0;
        long operations = 0;
        int rnd = (int)System.nanoTime();
        int rndMask = startRndMask;
        long time = 0;
        int currentStride = 0;
        do {
            if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                try {
                    if (control.isFailing) throw new FailureAssistException();
                    if (!l_estadodobenchmark1_G.readyInvocation) {
                        l_estadodobenchmark1_G.setupInvocation();
                        l_estadodobenchmark1_G.readyInvocation = true;
                    }
                } catch (Throwable t) {
                    control.isFailing = true;
                    throw t;
                } finally {
                    IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                }
            } else {
                while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                    if (control.isFailing) throw new FailureAssistException();
                    if (Thread.interrupted()) throw new InterruptedException();
                }
            }
            long rt = System.nanoTime();
            rnd = (rnd * 1664525 + 1013904223);
            boolean sample = (rnd & rndMask) == 0;
            if (sample) {
                time = System.nanoTime();
            }
            for (int b = 0; b < batchSize; b++) {
                if (control.volatileSpoiler) return;
                l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
            }
            if (sample) {
                buffer.add((System.nanoTime() - time) / opsPerInv);
                if (currentStride++ > targetSamples) {
                    buffer.half();
                    currentStride = 0;
                    rndMask = (rndMask << 1) + 1;
                }
            }
            realTime += (System.nanoTime() - rt);
            if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                try {
                    if (control.isFailing) throw new FailureAssistException();
                    if (l_estadodobenchmark1_G.readyInvocation) {
                        l_estadodobenchmark1_G.readyInvocation = false;
                    }
                } catch (Throwable t) {
                    control.isFailing = true;
                    throw t;
                } finally {
                    IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                }
            } else {
                while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                    if (control.isFailing) throw new FailureAssistException();
                    if (Thread.interrupted()) throw new InterruptedException();
                }
            }
            operations++;
        } while(!control.isDone);
        startRndMask = Math.max(startRndMask, rndMask);
        result.realTime = realTime;
        result.measuredOps = operations;
    }


    public BenchmarkTaskResult benchmarkProcessarLote_SingleShotTime(InfraControl control, ThreadParams threadParams) throws Throwable {
        this.benchmarkParams = control.benchmarkParams;
        this.iterationParams = control.iterationParams;
        this.threadParams    = threadParams;
        this.notifyControl   = control.notifyControl;
        if (this.blackhole == null) {
            this.blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        }
        if (threadParams.getSubgroupIndex() == 0) {
            IDWBenchmark_jmhType l_idwbenchmark0_0 = _jmh_tryInit_f_idwbenchmark0_0(control);
            IDWBenchmark_EstadoDoBenchmark_jmhType l_estadodobenchmark1_G = _jmh_tryInit_f_estadodobenchmark1_G(control);

            control.preSetup();


            notifyControl.startMeasurement = true;
            RawResults res = new RawResults();
            int batchSize = iterationParams.getBatchSize();
            benchmarkProcessarLote_ss_jmhStub(control, res, benchmarkParams, iterationParams, threadParams, blackhole, notifyControl, startRndMask, batchSize, l_estadodobenchmark1_G, l_idwbenchmark0_0);
            control.preTearDown();

            if (control.isLastIteration()) {
                if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                    try {
                        if (control.isFailing) throw new FailureAssistException();
                        if (l_estadodobenchmark1_G.readyTrial) {
                            l_estadodobenchmark1_G.tearDownTrial();
                            l_estadodobenchmark1_G.readyTrial = false;
                        }
                    } catch (Throwable t) {
                        control.isFailing = true;
                        throw t;
                    } finally {
                        IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.set(l_estadodobenchmark1_G, 0);
                    }
                } else {
                    long l_estadodobenchmark1_G_backoff = 1;
                    while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearTrialMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                        TimeUnit.MILLISECONDS.sleep(l_estadodobenchmark1_G_backoff);
                        l_estadodobenchmark1_G_backoff = Math.max(1024, l_estadodobenchmark1_G_backoff * 2);
                        if (control.isFailing) throw new FailureAssistException();
                        if (Thread.interrupted()) throw new InterruptedException();
                    }
                }
                synchronized(this.getClass()) {
                    f_estadodobenchmark1_G = null;
                }
                f_idwbenchmark0_0 = null;
            }
            int opsPerInv = control.benchmarkParams.getOpsPerInvocation();
            long totalOps = opsPerInv;
            BenchmarkTaskResult results = new BenchmarkTaskResult(totalOps, totalOps);
            results.add(new SingleShotResult(ResultRole.PRIMARY, "benchmarkProcessarLote", res.getTime(), totalOps, benchmarkParams.getTimeUnit()));
            this.blackhole.evaporate("Yes, I am Stephen Hawking, and know a thing or two about black holes.");
            return results;
        } else
            throw new IllegalStateException("Harness failed to distribute threads among groups properly");
    }

    public static void benchmarkProcessarLote_ss_jmhStub(InfraControl control, RawResults result, BenchmarkParams benchmarkParams, IterationParams iterationParams, ThreadParams threadParams, Blackhole blackhole, Control notifyControl, int startRndMask, int batchSize, IDWBenchmark_EstadoDoBenchmark_jmhType l_estadodobenchmark1_G, IDWBenchmark_jmhType l_idwbenchmark0_0) throws Throwable {
        long realTime = 0;
        result.startTime = System.nanoTime();
        for (int b = 0; b < batchSize; b++) {
            if (control.volatileSpoiler) return;
            if (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                try {
                    if (control.isFailing) throw new FailureAssistException();
                    if (!l_estadodobenchmark1_G.readyInvocation) {
                        l_estadodobenchmark1_G.setupInvocation();
                        l_estadodobenchmark1_G.readyInvocation = true;
                    }
                } catch (Throwable t) {
                    control.isFailing = true;
                    throw t;
                } finally {
                    IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                }
            } else {
                while (IDWBenchmark_EstadoDoBenchmark_jmhType.setupInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                    if (control.isFailing) throw new FailureAssistException();
                    if (Thread.interrupted()) throw new InterruptedException();
                }
            }
            long rt = System.nanoTime();
            l_idwbenchmark0_0.benchmarkProcessarLote(l_estadodobenchmark1_G, blackhole);
            realTime += (System.nanoTime() - rt);
            if (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.compareAndSet(l_estadodobenchmark1_G, 0, 1)) {
                try {
                    if (control.isFailing) throw new FailureAssistException();
                    if (l_estadodobenchmark1_G.readyInvocation) {
                        l_estadodobenchmark1_G.readyInvocation = false;
                    }
                } catch (Throwable t) {
                    control.isFailing = true;
                    throw t;
                } finally {
                    IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.set(l_estadodobenchmark1_G, 0);
                }
            } else {
                while (IDWBenchmark_EstadoDoBenchmark_jmhType.tearInvocationMutexUpdater.get(l_estadodobenchmark1_G) == 1) {
                    if (control.isFailing) throw new FailureAssistException();
                    if (Thread.interrupted()) throw new InterruptedException();
                }
            }
        }
        result.stopTime = System.nanoTime();
        result.realTime = realTime;
    }

    
    static volatile IDWBenchmark_EstadoDoBenchmark_jmhType f_estadodobenchmark1_G;
    
    IDWBenchmark_EstadoDoBenchmark_jmhType _jmh_tryInit_f_estadodobenchmark1_G(InfraControl control) throws Throwable {
        IDWBenchmark_EstadoDoBenchmark_jmhType val = f_estadodobenchmark1_G;
        if (val != null) {
            return val;
        }
        synchronized(this.getClass()) {
            try {
            if (control.isFailing) throw new FailureAssistException();
            val = f_estadodobenchmark1_G;
            if (val != null) {
                return val;
            }
            val = new IDWBenchmark_EstadoDoBenchmark_jmhType();
            val.setupTrial();
            val.readyTrial = true;
            f_estadodobenchmark1_G = val;
            } catch (Throwable t) {
                control.isFailing = true;
                throw t;
            }
        }
        return val;
    }
    
    IDWBenchmark_jmhType f_idwbenchmark0_0;
    
    IDWBenchmark_jmhType _jmh_tryInit_f_idwbenchmark0_0(InfraControl control) throws Throwable {
        if (control.isFailing) throw new FailureAssistException();
        IDWBenchmark_jmhType val = f_idwbenchmark0_0;
        if (val == null) {
            val = new IDWBenchmark_jmhType();
            f_idwbenchmark0_0 = val;
        }
        return val;
    }


}

