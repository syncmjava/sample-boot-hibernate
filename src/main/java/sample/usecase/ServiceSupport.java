package sample.usecase;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import lombok.Setter;
import sample.context.DomainHelper;
import sample.context.actor.Actor;
import sample.context.audit.AuditHandler;
import sample.context.lock.IdLockHandler;
import sample.context.lock.IdLockHandler.LockType;
import sample.context.orm.DefaultRepository;
import sample.model.BusinessDayHandler;
import sample.usecase.mail.ServiceMailDeliver;
import sample.usecase.report.ServiceReportExporter;

/**
 * ユースケースサービスの基底クラス。
 */
@Setter
public abstract class ServiceSupport {

    @Autowired
    private MessageSource msg;

    @Autowired
    private DomainHelper dh;
    @Autowired
    private DefaultRepository rep;
    @Autowired
    @Qualifier(DefaultRepository.BeanNameTx)
    private PlatformTransactionManager tx;
    @Autowired
    private IdLockHandler idLock;

    @Autowired
    private AuditHandler audit;
    @Autowired(required = false)
    private BusinessDayHandler businessDay;

    @Autowired(required = false)
    private ServiceMailDeliver mail;
    @Autowired(required = false)
    private ServiceReportExporter report;

    /** トランザクション処理を実行します。 */
    protected <T> T tx(Supplier<T> callable) {
        return ServiceUtils.tx(tx, callable);
    }

    /** トランザクション処理を実行します。 */
    protected void tx(Runnable command) {
        ServiceUtils.tx(tx, command);
    }

    /** 口座ロック付でトランザクション処理を実行します。 */
    protected <T> T tx(String accountId, LockType lockType, final Supplier<T> callable) {
        return idLock.call(accountId, lockType, () -> {
            return tx(callable);
        });
    }

    /** 口座ロック付でトランザクション処理を実行します。 */
    protected void tx(String accountId, LockType lockType, final Runnable callable) {
        idLock.call(accountId, lockType, () -> {
            tx(callable);
            return true;
        });
    }

    /** ドメイン層向けヘルパークラスを返します。 */
    protected DomainHelper dh() {
        return dh;
    }

    /** 標準スキーマのRepositoryを返します。 */
    protected DefaultRepository rep() {
        return rep;
    }

    /** IDロックユーティリティを返します。 */
    protected IdLockHandler idLock() {
        return idLock;
    }

    /** 監査ユーティリティを返します。 */
    protected AuditHandler audit() {
        return audit;
    }

    /** サービスメールユーティリティを返します。 */
    protected ServiceMailDeliver mail() {
        Assert.notNull(mail, "mail is not setup.");
        return mail;
    }

    /** サービスレポートユーティリティを返します。 */
    protected ServiceReportExporter report() {
        Assert.notNull(report, "report is not setup.");
        return report;
    }

    /** i18nメッセージ変換を行います。 */
    protected String msg(String message) {
        return msg.getMessage(message, null, message, actor().getLocale());
    }

    /** 利用者を返します。 */
    protected Actor actor() {
        return dh.actor();
    }

    /** 営業日ユーティリティを返します。 */
    protected BusinessDayHandler businessDay() {
        Assert.notNull(businessDay, "businessDay is not setup.");
        return businessDay;
    }

}
