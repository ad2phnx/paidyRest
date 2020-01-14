[
  '{{repeat(20, 100)}}',
  {
    table: 't{{integer(1, 100)}}',
    name: function (tags) {
      var meals = ['pizza',
                   'hamburger',
                   'spaghetti',
                   'pot_roast',
                   'cobb_salad',
                   'key_lime_pie',
                   'tater_tots',
                   'banana_split',
                   'jambalaya',
                   'biscuits_n_gravy',
                   'chicken_fried_steak',
                   'salmon',
                   'meatloaf',
                   'grits',
                   'macaroni_and_cheese',
                   'chips',
                   'peanut_butter_sandwich',
                   'baked_beans',
                   'clam_chowder',
                   'buffalo_wings',
                   'indian_frybread',
                   'barbecue_ribs',
                   'blt',
                   'apple_pie',
                   'steak',
                   'blueberry_pie',
                   'nachos',
                   'philly_cheese_steak',
                   'hot_dogs'
                  ];
      return meals[tags.integer(0, meals.length - 1)];
    }
  }
]
