-- SQL migration for TBE-2231: Update grid configs for new package names
-- warning! This migration may only run once!
UPDATE p28_cfg_lean_grid_config
SET grid_prefs = decode(
    replace(
        encode(grid_prefs, 'escape'),
        'ui.UIFilter',
        't9t.base.ui.UIFilter'
    ),
    'escape'
)
WHERE position(
    decode('ui.UIFilter', 'escape') IN grid_prefs
) > 0;
