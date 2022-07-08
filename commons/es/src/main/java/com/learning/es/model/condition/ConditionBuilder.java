package com.learning.es.model.condition;

import com.learning.core.utils.StringUtils;
import com.learning.es.enums.LogicEnum;
import com.learning.es.enums.RelativeTypeEnum;
import com.learning.es.utils.tree.RegionBeanTree;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.join.query.JoinQueryBuilders;

import java.util.*;

/**
 * 查询条件构造类
 */
@Log4j2
public class ConditionBuilder {
    /**
     * 布尔逻辑
     */
    private LogicEnum logical;
    /**
     * 条件列表
     */
    private List<QueryCondition> queryBuilders = new ArrayList<>();
    /**
     * 子条件列表
     */
    private List<ConditionBuilder> child;
    private Map<String, PropertyMapper> mapperMap;
    private Map<String, String> childParentMap;
    Map<Integer, List<String>> levelTypeMap;
    private List<RegionBeanTree> typeTree;

    public ConditionBuilder(
            Map<String, PropertyMapper> mapperMap,
            Map<String, String> typeMap,
            List<RegionBeanTree> typeTree
    ) {
        this.mapperMap = mapperMap;
        this.typeTree = typeTree;
        this.childParentMap = new HashMap<>();
        this.levelTypeMap = new HashMap<>();
        if (typeTree != null) {
            this.getTreeMap(1, typeTree, (String)null);
        }

        log.info(this.childParentMap.size());
    }

    public Map<String, PropertyMapper> getMapperMap() {
        return this.mapperMap;
    }

    public LogicEnum getLogical() {
        return this.logical;
    }

    public void setLogical(LogicEnum logical) {
        this.logical = logical;
    }

    public List<QueryCondition> getQueryBuilders() {
        return this.queryBuilders;
    }

    public void setQueryBuilders(List<QueryCondition> queryBuilders) {
        this.queryBuilders = queryBuilders;
    }

    public List<ConditionBuilder> getChild() {
        return this.child;
    }

    public void setChild(List<ConditionBuilder> child) {
        this.child = child;
    }

    public void addChild(ConditionBuilder cb) {
        if (this.child == null) {
            this.child = new ArrayList<>();
        }

        this.child.add(cb);
    }

    /**
     * 增加查询条件
     * @param builder 查询条件
     */
    public void addQueryBuilder(QueryCondition builder) {
        if (builder != null) {
            if (this.queryBuilders == null) {
                this.queryBuilders = new ArrayList<>();
            }

            this.queryBuilders.add(builder);
        }
    }

    /**
     * 增加查询条件
     * @param builders 查询条件列表
     */
    public void addQueryBuilders(List<QueryCondition> builders) {
        if (builders != null && builders.size() != 0) {
            if (this.queryBuilders == null) {
                this.queryBuilders = new ArrayList<>();
            }

            this.queryBuilders.addAll(builders);
        }
    }

    /**
     * 构造查询构造器
     * @param level 层级
     * @return
     */
    public QueryBuilder toQueryBuilder(int level) {
        QueryBuilder qb = null;
        List<QueryBuilder> qbList = new ArrayList<>();
        List<ConditionBuilder> newChild = new ArrayList<>();

        //通过此对象，构造相关的查询条件
        if (this.queryBuilders != null && this.queryBuilders.size() > 0) {
            ConditionBuilder.Mapper mapper = new ConditionBuilder.Mapper();
            mapper.setLogical(this.logical);
            mapper.push(this.queryBuilders);
            //构建子查询条件
            if (this.child != null && this.child.size() > 0
            ) {
                Iterator<ConditionBuilder> it = this.child.iterator();

                label65:
                while(true) {
                    while(true) {
                        ConditionBuilder childConditionBuilder;
                        do {
                            if (!it.hasNext()) {
                                break label65;
                            }

                            childConditionBuilder = it.next();
                        } while(childConditionBuilder.queryBuilders == null);

                        if (childConditionBuilder.queryBuilders.size() != 1 && ! childConditionBuilder.logical.equals(this.logical)) {
                            newChild.add(childConditionBuilder);
                        } else {
                            mapper.push(childConditionBuilder.queryBuilders);
                        }
                    }
                }
            }

            List<QueryBuilder> bqbs = null;

            if (level == 1) {
                bqbs = mapper.getRootQuery(
                        this.childParentMap,
                        this.levelTypeMap,
                        null
                );
            } else if (level == 2) {
                bqbs = mapper.getSecondQuery(
                        this.childParentMap,
                        this.levelTypeMap,
                        null
                );
            } else if (level == 3) {
                bqbs = mapper.getThirdQuery(
                        this.childParentMap,
                        this.levelTypeMap,
                        null
                );
            }

            if (bqbs != null) {
                qbList.addAll(bqbs);
            }
        }

        if (newChild.size() > 0) {

            for (ConditionBuilder c : newChild) {
                QueryBuilder childQueryBuilder = c.toQueryBuilder(level);

                if (childQueryBuilder != null) {
                    qbList.add(childQueryBuilder);
                }
            }
        }

        if (qbList.size() > 0) {
            qb = ConditionBuildBase.createBoolQuery(qbList, this.logical);
        }

        return qb;
    }

    /**
     * 递归获取树
     * @param curL 对象层级
     * @param typeTree 类型树节点列表
     * @param parentType 父类型
     */
    private void getTreeMap(
            Integer curL,
            List<RegionBeanTree> typeTree,
            String parentType
    ) {

        for (RegionBeanTree tree : typeTree) {
            List<String> curLevelMap = this.levelTypeMap.get(curL);

            if (curLevelMap == null) {
                curLevelMap = new ArrayList<>();
            }

            String curType = tree.getCode();
            curLevelMap.add(curType);
            this.levelTypeMap.put(curL, curLevelMap);
            this.childParentMap.put(curType, parentType);
            //获取子节点
            if (tree.getChildren() != null) {
                Integer childL = curL + 1;
                this.getTreeMap(childL, tree.getChildren(), curType);
            }
        }

    }

    /**
     * 查询对应类
     */
    private static class Mapper {
        //逻辑词
        private LogicEnum logical;
        //查询map
        private HashMap<String, List<QueryCondition>> queryMap;

        private Mapper() {
        }

        public void setLogical(LogicEnum logical) {
            this.logical = logical;
        }

        //添加查询条件
        public void push(QueryCondition queryCondition) {

            if (this.queryMap == null) {
                this.queryMap = new HashMap<>();
            }

            //获取查询类型
            String curType = queryCondition
                    .getFieldItemInfo()
                    .getTypeName();
            //获取已存储的查询条件
            List<QueryCondition> curs = this
                    .queryMap
                    .get(curType);

            if (curs == null) {
                curs = new ArrayList<>();
            }

            if (! StringUtils.isEmpty(curType)) {
                curs.add(queryCondition);
                this.queryMap.put(curType, curs);
            }
        }

        public void push(List<QueryCondition> queryConditions) {

            for (QueryCondition qc : queryConditions) {
                this.push(qc);
            }

        }

        /**
         * 获取根查询构造器
         * @param childParentMap 父子对应map
         * @param levelTypeMap 层级类型对应map
         * @param rootLevel 根层级
         * @return
         */
        public List<QueryBuilder> getRootQuery(Map<String, String> childParentMap, Map<Integer, List<String>> levelTypeMap, Integer rootLevel) {
            //设置根层级数字
            rootLevel = rootLevel == null ? 1 : rootLevel;
            Integer current = 1;
            List<QueryBuilder> queryBuilders = new ArrayList<>();
            Integer childLevel = rootLevel + 1;
            //获取根对应的类型
            List<String> types = levelTypeMap.get(rootLevel);

            for (String type : types) {

                List<QueryBuilder> childBuilders = getQuery(type);

                if (rootLevel < 4) {
                    boolean isCurr = current.equals(rootLevel);
                    List<QueryBuilder> childQ = this.getRootQuery(childParentMap, levelTypeMap, childLevel);
                    if (childQ != null && childQ.size() > 0) {
                        childBuilders.addAll(childQ);
                    }

                    if (!isCurr) {
                        QueryBuilder curQ = ConditionBuildBase.createBoolQuery(childBuilders, this.logical);
                        curQ = this.getJoinQuery(type, curQ, true);
                        if (curQ != null) {
                            queryBuilders.add(curQ);
                        }
                    } else {
                        queryBuilders.addAll(childBuilders);
                    }
                }
            }

            return queryBuilders;
        }

        /**
         * 获取二级查询构造器数组
         * @param childParentMap 父子对应map
         * @param levelTypeMap 层次与类型数组对应map
         * @param rootLevel 根层次
         * @return
         */
        public List<QueryBuilder> getSecondQuery(
                Map<String, String> childParentMap,
                Map<Integer, List<String>> levelTypeMap,
                Integer rootLevel
        ) {
            rootLevel = rootLevel == null ? 1 : rootLevel;
            Integer current = 2;
            List<QueryBuilder> queryBuilders = new ArrayList<>();
            Integer childLevel = rootLevel + 1;
            //获取根类型数组
            List<String> types = levelTypeMap.get(rootLevel);

            for (String type : types) {

                List<QueryBuilder> childBuilders = getQuery(type);

                if (rootLevel < 4) {
                    boolean isCurr = current.equals(rootLevel);

                    //构造关联查询构造器
                    if (rootLevel.compareTo(current) < 0) {
                        QueryBuilder curQ = this.getJoinQuery(
                                type,
                                ConditionBuildBase.createBoolQuery(
                                        childBuilders,
                                        this.logical
                                ),
                                false
                        );

                        if (curQ != null) {
                            queryBuilders.add(curQ);
                        }
                    }

                    //构造子查询构造器
                    List<QueryBuilder> childQ = this.getSecondQuery(childParentMap, levelTypeMap, childLevel);

                    if (childQ != null && childQ.size() > 0) {
                        childBuilders.addAll(childQ);
                    }

                    if (! isCurr) {
                        QueryBuilder curQ = ConditionBuildBase.createBoolQuery(childBuilders, this.logical);

                        if (rootLevel.compareTo(current) > 0) {
                            curQ = this.getJoinQuery(type, curQ, true);
                            if (curQ != null) {
                                queryBuilders.add(curQ);
                            }
                        } else {
                            queryBuilders.addAll(childQ);
                        }
                    } else {
                        queryBuilders.addAll(childBuilders);
                    }
                }
            }

            return queryBuilders;
        }

        public List<QueryBuilder> getThirdQuery(Map<String, String> childParentMap, Map<Integer, List<String>> levelTypeMap, Integer rootLevel) {

            int current = 3;
            List<QueryBuilder> queryBuilders = new ArrayList<>();
            List<QueryBuilder> secondQuery = this.getSecondQuery(childParentMap, levelTypeMap, rootLevel);

            if (secondQuery != null && secondQuery.size() > 0) {
                QueryBuilder curQ = ConditionBuildBase.createBoolQuery(secondQuery, this.logical);
                List<String> types = levelTypeMap.get(current - 1);
                List<QueryBuilder> parentQuery = new ArrayList<>();

                for (String type : types) {
                    curQ = this.getJoinQuery(type, curQ, false);
                    if (curQ != null) {
                        parentQuery.add(curQ);
                    }
                }

                QueryBuilder newQ = ConditionBuildBase.createBoolQuery(parentQuery, LogicEnum.OR);

                if (newQ != null) {
                    queryBuilders.add(newQ);
                }
            }

            return queryBuilders;
        }

        public List<QueryBuilder> getThirdQuery1(Map<String, String> childParentMap, Map<Integer, List<String>> levelTypeMap, Integer rootLevel) {
            rootLevel = rootLevel == null ? 1 : rootLevel;
            Integer current = 3;
            List<QueryBuilder> queryBuilders = new ArrayList<>();
            Integer childLevel = rootLevel + 1;
            List<String> types = levelTypeMap.get(rootLevel);
            if (types != null) {

                for (String type : types) {
                    List<QueryCondition> curTypeQueryArr = this.queryMap.get(type);
                    List<QueryBuilder> childBuilders = new ArrayList<>();
                    QueryBuilder curQ;

                    if (curTypeQueryArr != null) {

                        for (QueryCondition qCond : curTypeQueryArr) {
                            curQ = ConditionBuildBase.createItemQuery(qCond.getFieldItemInfo(), qCond.getRelative(), qCond.getValue(), qCond.getEndValue());
                            if (curQ != null) {
                                childBuilders.add(curQ);
                            }
                        }
                    }

                    if (rootLevel < 4) {
                        boolean isCurr = current.equals(rootLevel);

                        if (rootLevel.compareTo(current) < 0) {
                            curQ = ConditionBuildBase.createBoolQuery(childBuilders, this.logical);
                            curQ = this.getJoinQuery(type, curQ, false);

                            if (curQ != null) {
                                queryBuilders.add(curQ);
                            }
                        }

                        List<QueryBuilder> childQ = this.getThirdQuery(childParentMap, levelTypeMap, childLevel);
                        if (childQ != null && childQ.size() > 0) {
                            childBuilders.addAll(childQ);
                        }

                        if (!isCurr) {
                            curQ = ConditionBuildBase.createBoolQuery(childBuilders, this.logical);
                            if (rootLevel.compareTo(current) > 0) {
                                curQ = this.getJoinQuery(type, curQ, true);
                                if (curQ != null) {
                                    queryBuilders.add(curQ);
                                }
                            } else {
                                queryBuilders.addAll(childQ);
                            }
                        } else {
                            queryBuilders.addAll(childBuilders);
                        }
                    }
                }
            }

            return queryBuilders;
        }

        /**
         * 构建父子关联查询构造器
         * @param type 关联文档类型
         * @param qb 关联查询构造器
         * @param isChild 是否为子查询
         * @return
         */
        private QueryBuilder getJoinQuery(String type, QueryBuilder qb, boolean isChild) {
            if (qb == null) {
                return null;
            } else {
                QueryBuilder b;
                //构造子级查询
                if (isChild) {
                    b = JoinQueryBuilders.hasChildQuery(type, qb, ScoreMode.None);
                }
                //构造父级查询
                else {
                    b = JoinQueryBuilders.hasParentQuery(type, qb, true);
                }

                return b;
            }
        }

        /**
         * 获取查询构造器数组
         * @param type 类型
         * @return
         */
        private List<QueryBuilder> getQuery(String type) {
            //获取当前查询数组
            List<QueryCondition> curTypeQueryArr = this.queryMap.get(type);
            List<QueryBuilder> childBuilders = new ArrayList<>();

            if (curTypeQueryArr != null) {

                for (QueryCondition curTypeQuery : curTypeQueryArr) {
                    BoolQueryBuilder itemBool = new BoolQueryBuilder();

                    //构造查询构造器
                    QueryBuilder qb = ConditionBuildBase
                            .createItemQuery(
                                    curTypeQuery.getFieldItemInfo(),
                                    curTypeQuery.getRelative(),
                                    curTypeQuery.getValue(),
                                    curTypeQuery.getEndValue()
                            );

                    //构造等于其余值查询构造器
                    String otherValue = curTypeQuery.getOtherValue() == null ? "" : curTypeQuery.getOtherValue().toString();
                    QueryBuilder otherQb = null;
                    if (
                            curTypeQuery.getOtherFieldItemInfo() != null
                                    && ! StringUtils.isEmpty(otherValue)
                    ) {
                        otherQb = ConditionBuildBase
                                .createItemQuery(
                                        curTypeQuery.getOtherFieldItemInfo(),
                                        RelativeTypeEnum.EQUAL.getCode(),
                                        curTypeQuery.getOtherValue(),
                                        null
                                );
                    }

                    //构造子查询构造器数组
                    if (qb != null) {

                        if (otherQb != null) {
                            itemBool.must(otherQb);
                        }

                        itemBool.must(qb);
                        childBuilders.add(itemBool);
                    }
                }
            }

            return childBuilders;
        }
    }
}