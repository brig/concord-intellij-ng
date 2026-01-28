const context = {
    /** @param {string} name */
    getVariable: function(name) {},
    /**
     * @param {string} name
     * @param {any} value
     **/
    setVariable: function(name, value) {},
    /**
     * @param {string} expression
     * @param {Object} [variables]
     **/
    eval: function(expression, variables) {},
    variables: function() {
        return {
            get: function(key) {},
            has: function(key) {},
            getString: function(key, defaultValue) {},
            assertString: function(key, message) {},
            getNumber: function(key, defaultValue) {},
            assertNumber: function(key) {},
            getBoolean: function(key, defaultValue) {},
            assertBoolean: function(key) {},
            getInt: function(key, defaultValue) {},
            assertInt: function(key) {},
            getLong: function(key, defaultValue) {},
            assertLong: function(key) {},
            getUUID: function(key) {},
            assertUUID: function(key) {},
            getCollection: function(key, defaultValue) {},
            assertCollection: function(key) {},
            getMap: function(key, defaultValue) {},
            assertMap: function(name) {},
            getList: function(key, defaultValue) {},
            assertList: function(key) {}
        };
    },
    workingDirectory: function() {},
    processInstanceId: function() {}
};

const execution = context;

const tasks = {};

const log = {
    /** @param {string} msg */
    info: function(msg) {},
    /** @param {string} msg */
    error: function(msg) {},
    /** @param {string} msg */
    warn: function(msg) {},
    /** @param {string} msg */
    debug: function(msg) {}
};

/** @type {boolean} */
const isDryRun = false;

const result = {
    /**
     * @param {string} key
     * @param {any} value
     **/
    set: function(key, value) {}
};
