package com.learning.es.bean;

import java.util.List;

/**
 * 高级搜索结果
 */
public class AdvancedSearchResult {
    /**
     * 结果数组
     */
    private List<Result> data;
    /**
     * 总结果数
     */
    private long total;

    public AdvancedSearchResult() {
    }

    public AdvancedSearchResult(List<AdvancedSearchResult.Result> data, long total) {
        this.data = data;
        this.total = total;
    }

    public static AdvancedSearchResult ok(List<AdvancedSearchResult.Result> data, long total) {
        return new AdvancedSearchResult(data, total);
    }

    public List<AdvancedSearchResult.Result> getData() {
        return this.data;
    }

    public long getTotal() {
        return this.total;
    }

    public void setData(List<AdvancedSearchResult.Result> data) {
        this.data = data;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof AdvancedSearchResult)) {
            return false;
        } else {
            AdvancedSearchResult other = (AdvancedSearchResult)o;
            if (other.canEqual(this)) {
                Object this$data = this.getData();
                Object other$data = other.getData();

                if (this$data == null) {
                    if (other$data == null) {
                        return this.getTotal() == other.getTotal();
                    }
                } else if (this$data.equals(other$data)) {
                    return this.getTotal() == other.getTotal();
                }

            }
            return false;
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof AdvancedSearchResult;
    }

    public int hashCode() {
        int result = 1;
        Object $data = this.getData();
        result = result * 59 + ($data == null ? 43 : $data.hashCode());
        long $total = this.getTotal();
        result = result * 59 + (int)($total >>> 32 ^ $total);
        return result;
    }

    public String toString() {
        return "AdvancedSearchResult(data=" + this.getData() + ", total=" + this.getTotal() + ")";
    }

    public static class Child {
        private String admNo;
        private String admType;
        private String admDept;
        private String admDate;
        private String dishDate;
        private String dishDept;
        private String diagName;
        private boolean isHit;

        public Child() {
        }

        public String getAdmNo() {
            return this.admNo;
        }

        public String getAdmType() {
            return this.admType;
        }

        public String getAdmDept() {
            return this.admDept;
        }

        public String getAdmDate() {
            return this.admDate;
        }

        public String getDishDate() {
            return this.dishDate;
        }

        public String getDishDept() {
            return this.dishDept;
        }

        public String getDiagName() {
            return this.diagName;
        }

        public boolean isHit() {
            return this.isHit;
        }

        public void setAdmNo(String admNo) {
            this.admNo = admNo;
        }

        public void setAdmType(String admType) {
            this.admType = admType;
        }

        public void setAdmDept(String admDept) {
            this.admDept = admDept;
        }

        public void setAdmDate(String admDate) {
            this.admDate = admDate;
        }

        public void setDishDate(String dishDate) {
            this.dishDate = dishDate;
        }

        public void setDishDept(String dishDept) {
            this.dishDept = dishDept;
        }

        public void setDiagName(String diagName) {
            this.diagName = diagName;
        }

        public void setHit(boolean isHit) {
            this.isHit = isHit;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof AdvancedSearchResult.Child)) {
                return false;
            } else {
                AdvancedSearchResult.Child other = (AdvancedSearchResult.Child)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$admNo = this.getAdmNo();
                    Object other$admNo = other.getAdmNo();
                    if (this$admNo == null) {
                        if (other$admNo != null) {
                            return false;
                        }
                    } else if (!this$admNo.equals(other$admNo)) {
                        return false;
                    }

                    Object this$admType = this.getAdmType();
                    Object other$admType = other.getAdmType();
                    if (this$admType == null) {
                        if (other$admType != null) {
                            return false;
                        }
                    } else if (!this$admType.equals(other$admType)) {
                        return false;
                    }

                    Object this$admDept = this.getAdmDept();
                    Object other$admDept = other.getAdmDept();
                    if (this$admDept == null) {
                        if (other$admDept != null) {
                            return false;
                        }
                    } else if (!this$admDept.equals(other$admDept)) {
                        return false;
                    }

                    label78: {
                        Object this$admDate = this.getAdmDate();
                        Object other$admDate = other.getAdmDate();
                        if (this$admDate == null) {
                            if (other$admDate == null) {
                                break label78;
                            }
                        } else if (this$admDate.equals(other$admDate)) {
                            break label78;
                        }

                        return false;
                    }

                    label71: {
                        Object this$dishDate = this.getDishDate();
                        Object other$dishDate = other.getDishDate();
                        if (this$dishDate == null) {
                            if (other$dishDate == null) {
                                break label71;
                            }
                        } else if (this$dishDate.equals(other$dishDate)) {
                            break label71;
                        }

                        return false;
                    }

                    Object this$dishDept = this.getDishDept();
                    Object other$dishDept = other.getDishDept();
                    if (this$dishDept == null) {
                        if (other$dishDept != null) {
                            return false;
                        }
                    } else if (!this$dishDept.equals(other$dishDept)) {
                        return false;
                    }

                    label57: {
                        Object this$diagName = this.getDiagName();
                        Object other$diagName = other.getDiagName();
                        if (this$diagName == null) {
                            if (other$diagName == null) {
                                break label57;
                            }
                        } else if (this$diagName.equals(other$diagName)) {
                            break label57;
                        }

                        return false;
                    }

                    if (this.isHit() != other.isHit()) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof AdvancedSearchResult.Child;
        }

        public int hashCode() {
            int result = 1;
            Object $admNo = this.getAdmNo();
            result = result * 59 + ($admNo == null ? 43 : $admNo.hashCode());
            Object $admType = this.getAdmType();
            result = result * 59 + ($admType == null ? 43 : $admType.hashCode());
            Object $admDept = this.getAdmDept();
            result = result * 59 + ($admDept == null ? 43 : $admDept.hashCode());
            Object $admDate = this.getAdmDate();
            result = result * 59 + ($admDate == null ? 43 : $admDate.hashCode());
            Object $dishDate = this.getDishDate();
            result = result * 59 + ($dishDate == null ? 43 : $dishDate.hashCode());
            Object $dishDept = this.getDishDept();
            result = result * 59 + ($dishDept == null ? 43 : $dishDept.hashCode());
            Object $diagName = this.getDiagName();
            result = result * 59 + ($diagName == null ? 43 : $diagName.hashCode());
            result = result * 59 + (this.isHit() ? 79 : 97);
            return result;
        }

        public String toString() {
            return "AdvancedSearchResult.Child(admNo=" + this.getAdmNo() + ", admType=" + this.getAdmType() + ", admDept=" + this.getAdmDept() + ", admDate=" + this.getAdmDate() + ", dishDate=" + this.getDishDate() + ", dishDept=" + this.getDishDept() + ", diagName=" + this.getDiagName() + ", isHit=" + this.isHit() + ")";
        }
    }

    /**
     * 搜索结果对象
     */
    public static class Result {
        private String regNo;
        private String patientName;
        private String patientGender;
        private String patientBirthday;
        private List<AdvancedSearchResult.Child> children;
        private int childTotal;

        public Result() {
        }

        public String getRegNo() {
            return this.regNo;
        }

        public String getPatientName() {
            return this.patientName;
        }

        public String getPatientGender() {
            return this.patientGender;
        }

        public String getPatientBirthday() {
            return this.patientBirthday;
        }

        public List<AdvancedSearchResult.Child> getChildren() {
            return this.children;
        }

        public int getChildTotal() {
            return this.childTotal;
        }

        public void setRegNo(String regNo) {
            this.regNo = regNo;
        }

        public void setPatientName(String patientName) {
            this.patientName = patientName;
        }

        public void setPatientGender(String patientGender) {
            this.patientGender = patientGender;
        }

        public void setPatientBirthday(String patientBirthday) {
            this.patientBirthday = patientBirthday;
        }

        public void setChildren(List<AdvancedSearchResult.Child> children) {
            this.children = children;
        }

        public void setChildTotal(int childTotal) {
            this.childTotal = childTotal;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof AdvancedSearchResult.Result)) {
                return false;
            } else {
                AdvancedSearchResult.Result other = (AdvancedSearchResult.Result)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    label75: {
                        Object this$regNo = this.getRegNo();
                        Object other$regNo = other.getRegNo();
                        if (this$regNo == null) {
                            if (other$regNo == null) {
                                break label75;
                            }
                        } else if (this$regNo.equals(other$regNo)) {
                            break label75;
                        }

                        return false;
                    }

                    Object this$patientName = this.getPatientName();
                    Object other$patientName = other.getPatientName();
                    if (this$patientName == null) {
                        if (other$patientName != null) {
                            return false;
                        }
                    } else if (!this$patientName.equals(other$patientName)) {
                        return false;
                    }

                    Object this$patientGender = this.getPatientGender();
                    Object other$patientGender = other.getPatientGender();
                    if (this$patientGender == null) {
                        if (other$patientGender != null) {
                            return false;
                        }
                    } else if (!this$patientGender.equals(other$patientGender)) {
                        return false;
                    }

                    label54: {
                        Object this$patientBirthday = this.getPatientBirthday();
                        Object other$patientBirthday = other.getPatientBirthday();
                        if (this$patientBirthday == null) {
                            if (other$patientBirthday == null) {
                                break label54;
                            }
                        } else if (this$patientBirthday.equals(other$patientBirthday)) {
                            break label54;
                        }

                        return false;
                    }

                    label47: {
                        Object this$children = this.getChildren();
                        Object other$children = other.getChildren();
                        if (this$children == null) {
                            if (other$children == null) {
                                break label47;
                            }
                        } else if (this$children.equals(other$children)) {
                            break label47;
                        }

                        return false;
                    }

                    if (this.getChildTotal() != other.getChildTotal()) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof AdvancedSearchResult.Result;
        }

        public int hashCode() {
            int result = 1;
            Object $regNo = this.getRegNo();
            result = result * 59 + ($regNo == null ? 43 : $regNo.hashCode());
            Object $patientName = this.getPatientName();
            result = result * 59 + ($patientName == null ? 43 : $patientName.hashCode());
            Object $patientGender = this.getPatientGender();
            result = result * 59 + ($patientGender == null ? 43 : $patientGender.hashCode());
            Object $patientBirthday = this.getPatientBirthday();
            result = result * 59 + ($patientBirthday == null ? 43 : $patientBirthday.hashCode());
            Object $children = this.getChildren();
            result = result * 59 + ($children == null ? 43 : $children.hashCode());
            result = result * 59 + this.getChildTotal();
            return result;
        }

        public String toString() {
            return "AdvancedSearchResult.Result(regNo=" + this.getRegNo() + ", patientName=" + this.getPatientName() + ", patientGender=" + this.getPatientGender() + ", patientBirthday=" + this.getPatientBirthday() + ", children=" + this.getChildren() + ", childTotal=" + this.getChildTotal() + ")";
        }
    }
}
