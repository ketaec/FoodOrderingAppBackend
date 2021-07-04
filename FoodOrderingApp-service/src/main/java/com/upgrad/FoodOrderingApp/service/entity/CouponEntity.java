package com.upgrad.FoodOrderingApp.service.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "coupon")
@NamedQueries({
        @NamedQuery(name = "getCouponByName", query = "SELECT c from CouponEntity c where c.coupon_name = :coupon_name"),
        @NamedQuery(name = "getCouponById",query = "SELECT c FROM  CouponEntity c WHERE c.uuid = :uuid"),
})
public class CouponEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "uuid")
    @NotNull
    @Size(max = 200)
    private String uuid;

    @Column(name = "coupon_name")
    @Size(max = 255)
    private String coupon_name;

    @Column(name = "percent")
    @NotNull
    private Integer percent;

    public CouponEntity() {
    }

    public CouponEntity(@NotNull @Size(max = 200) String uuid, @Size(max = 255) String coupon_name, @NotNull Integer percent) {
        this.uuid = uuid;
        this.coupon_name = coupon_name;
        this.percent = percent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCoupon_name() {
        return coupon_name;
    }

    public void setCoupon_name(String coupon_name) {
        this.coupon_name = coupon_name;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public Integer getPercent() {
        return percent;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return new EqualsBuilder().append(this,obj).isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
